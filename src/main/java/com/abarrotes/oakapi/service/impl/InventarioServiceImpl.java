package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.AjusteInventarioRequestDto;
import com.abarrotes.oakapi.dto.response.AlertaCaducidadDto;
import com.abarrotes.oakapi.dto.response.AlertasCaducidadResponseDto;
import com.abarrotes.oakapi.dto.response.LoteResponseDto;
import com.abarrotes.oakapi.dto.response.MovimientoInventarioResponseDto;
import com.abarrotes.oakapi.dto.response.ValorizacionResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Lote;
import com.abarrotes.oakapi.model.MovimientoInventario;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.DocumentoOrigen;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import com.abarrotes.oakapi.repository.LoteRepository;
import com.abarrotes.oakapi.repository.MovimientoInventarioRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.repository.ValorizacionCategoriaProjection;
import com.abarrotes.oakapi.service.InventarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InventarioServiceImpl implements InventarioService {

    private static final Set<TipoMovimientoInventario> TIPOS_MANUALES_PERMITIDOS =
            Set.of(TipoMovimientoInventario.AJUSTE_POSITIVO,
                   TipoMovimientoInventario.AJUSTE_NEGATIVO,
                   TipoMovimientoInventario.MERMA);

    private static final int DIAS_ROJO = 15;
    private static final int DIAS_AMARILLO = 30;

    private final LoteRepository loteRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public List<LoteResponseDto> lotesPorProducto(Integer productoId) {
        return loteRepository.findLotesDisponiblesFEFO(productoId).stream()
                .map(this::mapToLoteResponse)
                .toList();
    }

    @Override
    @Transactional
    public MovimientoInventarioResponseDto registrarAjuste(AjusteInventarioRequestDto request) {
        TipoMovimientoInventario tipo = request.getTipoMovimiento();
        if (!TIPOS_MANUALES_PERMITIDOS.contains(tipo)) {
            throw new BusinessRuleException(
                    "Solo se permiten movimientos manuales de tipo AJUSTE_POSITIVO, AJUSTE_NEGATIVO o MERMA");
        }

        Lote lote = obtenerLoteConLock(request.getLoteId());
        BigDecimal anterior = lote.getCantidadActual();

        BigDecimal nueva;
        if (tipo == TipoMovimientoInventario.AJUSTE_POSITIVO) {
            nueva = anterior.add(request.getCantidad());
            if (nueva.compareTo(lote.getCantidadInicial()) > 0) {
                throw new BusinessRuleException(String.format(
                        "El ajuste excedería la cantidad inicial del lote (inicial: %s)", lote.getCantidadInicial()));
            }
        } else {
            nueva = anterior.subtract(request.getCantidad());
            if (nueva.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException(String.format(
                        "Stock insuficiente en el lote %s. Disponible: %s, solicitado: %s",
                        lote.getCodigoLote(), anterior, request.getCantidad()));
            }
        }

        lote.setCantidadActual(nueva);
        MovimientoInventario movimiento = guardarMovimiento(lote, obtenerUsuarioActual(), tipo,
                request.getCantidad(), anterior, nueva, request.getMotivo(), DocumentoOrigen.MANUAL, null);
        return mapToResponse(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertasCaducidadResponseDto alertasCaducidad(Integer dias) {
        validarDias(dias);
        LocalDate hoy = LocalDate.now();
        List<AlertaCaducidadDto> alertas = loteRepository.findLotesPorVencer(hoy.plusDays(dias)).stream()
                .map(lote -> mapToAlerta(lote, hoy))
                .toList();

        return AlertasCaducidadResponseDto.builder()
                .diasConsulta(dias)
                .totalAlertas((long) alertas.size())
                .enRojo(alertas.stream().filter(a -> a.getNivel() == AlertaCaducidadDto.NivelCaducidad.ROJO).count())
                .enAmarillo(alertas.stream().filter(a -> a.getNivel() == AlertaCaducidadDto.NivelCaducidad.AMARILLO).count())
                .valorEnRiesgoTotal(alertas.stream()
                        .map(AlertaCaducidadDto::getValorEnRiesgo)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .alertas(alertas)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ValorizacionResponseDto valorizacion() {
        List<ValorizacionCategoriaProjection> proyecciones = loteRepository.valorizarPorCategoria();
        List<ValorizacionResponseDto.ValorPorCategoriaDto> detalle = proyecciones.stream()
                .map(p -> ValorizacionResponseDto.ValorPorCategoriaDto.builder()
                        .categoria(p.getCategoria())
                        .valor(p.getValor().setScale(2, RoundingMode.HALF_UP))
                        .lotes(p.getLotes())
                        .build())
                .toList();

        return ValorizacionResponseDto.builder()
                .valorTotal(detalle.stream()
                        .map(ValorizacionResponseDto.ValorPorCategoriaDto::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .totalLotes(detalle.stream()
                        .mapToLong(ValorizacionResponseDto.ValorPorCategoriaDto::getLotes)
                        .sum())
                .detalle(detalle)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovimientoInventarioResponseDto> historial(Integer loteId, Integer productoId,
                                                           TipoMovimientoInventario tipo, Pageable pageable) {
        return movimientoRepository.buscarConFiltros(loteId, productoId, tipo, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional
    public List<MovimientoInventarioResponseDto> consumirStockFEFO(Integer productoId, BigDecimal cantidad,
                                                                   Integer usuarioId, String documentoVentaId) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La cantidad a consumir debe ser mayor a cero");
        }
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + usuarioId));

        List<Lote> lotes = loteRepository.findLotesDisponiblesFEFOConLock(productoId);
        BigDecimal disponibleTotal = lotes.stream()
                .map(Lote::getCantidadActual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (disponibleTotal.compareTo(cantidad) < 0) {
            throw new BusinessRuleException(String.format(
                    "Stock insuficiente para el producto %s. Disponible: %s, solicitado: %s",
                    productoId, disponibleTotal, cantidad));
        }

        List<MovimientoInventarioResponseDto> movimientos = new ArrayList<>();
        BigDecimal pendiente = cantidad;
        for (Lote lote : lotes) {
            if (pendiente.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal disponible = lote.getCantidadActual();
            BigDecimal tomar = disponible.min(pendiente);
            BigDecimal nueva = disponible.subtract(tomar);
            lote.setCantidadActual(nueva);
            movimientos.add(mapToResponse(guardarMovimiento(lote, usuario, TipoMovimientoInventario.SALIDA_VENTA,
                    tomar, disponible, nueva, null, DocumentoOrigen.VENTA, documentoVentaId)));
            pendiente = pendiente.subtract(tomar);
        }
        return movimientos;
    }

    private Lote obtenerLoteConLock(Integer loteId) {
        Lote lote = loteRepository.findByIdConLock(loteId);
        if (lote == null) {
            throw new ResourceNotFoundException("Lote no encontrado con ID: " + loteId);
        }
        return lote;
    }

    private Usuario obtenerUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    private void validarDias(Integer dias) {
        if (dias == null || dias < 1 || dias > 365) {
            throw new BusinessRuleException("El parámetro días debe estar entre 1 y 365");
        }
    }

    private MovimientoInventario guardarMovimiento(Lote lote, Usuario usuario, TipoMovimientoInventario tipo,
                                                   BigDecimal cantidad, BigDecimal anterior, BigDecimal nueva,
                                                   String motivo, DocumentoOrigen documentoTipo, String documentoId) {
        return movimientoRepository.save(MovimientoInventario.builder()
                .lote(lote)
                .usuario(usuario)
                .tipoMovimiento(tipo)
                .cantidad(cantidad)
                .cantidadAnterior(anterior)
                .cantidadNueva(nueva)
                .motivo(motivo)
                .documentoTipo(documentoTipo)
                .documentoId(documentoId)
                .build());
    }

    private AlertaCaducidadDto mapToAlerta(Lote lote, LocalDate hoy) {
        long diasRestantes = ChronoUnit.DAYS.between(hoy, lote.getFechaCaducidad());
        AlertaCaducidadDto.NivelCaducidad nivel = diasRestantes <= DIAS_ROJO
                ? AlertaCaducidadDto.NivelCaducidad.ROJO
                : diasRestantes <= DIAS_AMARILLO ? AlertaCaducidadDto.NivelCaducidad.AMARILLO
                : AlertaCaducidadDto.NivelCaducidad.VERDE;

        Producto producto = lote.getProducto();
        return AlertaCaducidadDto.builder()
                .loteId(lote.getId())
                .codigoLote(lote.getCodigoLote())
                .productoId(producto.getId())
                .plu(producto.getPlu())
                .productoNombre(producto.getNombre())
                .cantidadActual(lote.getCantidadActual())
                .costoCompra(lote.getCostoCompra())
                .fechaCaducidad(lote.getFechaCaducidad())
                .diasRestantes(diasRestantes)
                .nivel(nivel)
                .valorEnRiesgo(lote.getCantidadActual().multiply(lote.getCostoCompra()).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private LoteResponseDto mapToLoteResponse(Lote lote) {
        Producto producto = lote.getProducto();
        return LoteResponseDto.builder()
                .id(lote.getId())
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .codigoLote(lote.getCodigoLote())
                .costoCompra(lote.getCostoCompra())
                .cantidadInicial(lote.getCantidadInicial())
                .cantidadActual(lote.getCantidadActual())
                .fechaRecepcion(lote.getFechaRecepcion())
                .fechaCaducidad(lote.getFechaCaducidad())
                .valorInventario(lote.getCantidadActual().multiply(lote.getCostoCompra()).setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private MovimientoInventarioResponseDto mapToResponse(MovimientoInventario m) {
        Lote lote = m.getLote();
        Producto producto = lote.getProducto();
        return MovimientoInventarioResponseDto.builder()
                .id(m.getId())
                .loteId(lote.getId())
                .codigoLote(lote.getCodigoLote())
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .username(m.getUsuario().getUsername())
                .tipoMovimiento(m.getTipoMovimiento())
                .cantidad(m.getCantidad())
                .cantidadAnterior(m.getCantidadAnterior())
                .cantidadNueva(m.getCantidadNueva())
                .motivo(m.getMotivo())
                .documentoTipo(m.getDocumentoTipo())
                .documentoId(m.getDocumentoId())
                .fechaHora(m.getFechaHora())
                .build();
    }
}
