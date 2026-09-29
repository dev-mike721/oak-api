package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.LineaVentaRequestDto;
import com.abarrotes.oakapi.dto.request.VentaRequestDto;
import com.abarrotes.oakapi.dto.response.DetalleVentaResponseDto;
import com.abarrotes.oakapi.dto.response.MovimientoInventarioResponseDto;
import com.abarrotes.oakapi.dto.response.VentaResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.*;
import com.abarrotes.oakapi.model.enums.DocumentoOrigen;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import com.abarrotes.oakapi.repository.*;
import com.abarrotes.oakapi.service.InventarioService;
import com.abarrotes.oakapi.service.VentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final DetalleVentaRepository detalleVentaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoCompuestoRepository productoCompuestoRepository;
    private final LoteRepository loteRepository;
    private final TurnoCajaRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final InventarioService inventarioService;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<VentaResponseDto> obtenerTodas(MetodoPago metodoPago, EstadoVenta estado, Integer usuarioId,
                                               Integer turnoId, LocalDate fechaDesde, LocalDate fechaHasta,
                                               Pageable pageable) {
        LocalDateTime desde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = fechaHasta != null ? fechaHasta.plusDays(1).atStartOfDay() : null;
        return ventaRepository.buscarConFiltros(metodoPago, estado, usuarioId, turnoId, desde, hasta, pageable)
                .map(this::mapToResponseSinDetalle);
    }

    @Override
    @Transactional(readOnly = true)
    public VentaResponseDto obtenerPorId(String uuid) {
        Venta venta = obtenerVenta(uuid);
        VentaResponseDto dto = mapToResponseSinDetalle(venta);
        dto.setDetalle(detalleVentaRepository.findByVentaUuidOrderByIdAsc(uuid).stream()
                .map(this::mapDetalleToResponse)
                .toList());
        return dto;
    }

    @Override
    @Transactional
    public VentaResponseDto registrar(VentaRequestDto request) {
        Usuario usuario = obtenerUsuarioActual();
        TurnoCaja turno = turnoRepository
                .findFirstByUsuarioIdAndEstadoOrderByIdDesc(usuario.getId(), EstadoTurno.ABIERTO)
                .orElseThrow(() -> new BusinessRuleException(
                        "No hay un turno de caja abierto para el usuario. Abre un turno antes de registrar ventas"));

        if (request.getMetodoPago() == MetodoPago.CREDITO && request.getClienteId() == null) {
            throw new BusinessRuleException("El método de pago CREDITO requiere un cliente");
        }
        if (request.getClienteId() != null) {
            validarClienteExiste(request.getClienteId());
        }

        String uuid = UUID.randomUUID().toString();
        BigDecimal total = BigDecimal.ZERO;
        List<DetalleVenta> detalles = new ArrayList<>();

        for (LineaVentaRequestDto linea : request.getDetalle()) {
            Producto producto = productoRepository.findByIdAndActivoTrue(linea.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + linea.getProductoId()));

            if (Boolean.TRUE.equals(producto.getEsCompuesto())) {
                total = total.add(registrarLineaCompuesta(producto, linea, uuid, usuario, detalles));
            } else {
                total = total.add(registrarLineaSimple(producto, linea, uuid, usuario, detalles));
            }
        }

        Venta venta = ventaRepository.save(Venta.builder()
                .uuid(uuid)
                .turno(turno)
                .usuario(usuario)
                .clienteId(request.getClienteId())
                .fechaHora(LocalDateTime.now())
                .metodoPago(request.getMetodoPago())
                .total(total)
                .estado(EstadoVenta.COMPLETADA)
                .build());

        detalles.forEach(d -> d.setVenta(venta));
        detalleVentaRepository.saveAll(detalles);

        return obtenerPorId(uuid);
    }

    @Override
    @Transactional
    public void cancelar(String uuid) {
        Venta venta = obtenerVenta(uuid);
        if (venta.getEstado() == EstadoVenta.CANCELADA) {
            throw new BusinessRuleException("La venta ya está cancelada");
        }

        Usuario usuario = obtenerUsuarioActual();
        for (DetalleVenta detalle : detalleVentaRepository.findByVentaUuidOrderByIdAsc(uuid)) {
            Lote lote = loteRepository.findByIdConLock(detalle.getLote().getId());
            if (lote == null) {
                throw new ResourceNotFoundException("Lote no encontrado con ID: " + detalle.getLote().getId());
            }
            BigDecimal anterior = lote.getCantidadActual();
            BigDecimal nueva = anterior.add(detalle.getCantidad());
            lote.setCantidadActual(nueva);
            guardarMovimiento(lote, usuario, TipoMovimientoInventario.AJUSTE_POSITIVO,
                    detalle.getCantidad(), anterior, nueva,
                    "Cancelación de venta " + uuid, DocumentoOrigen.VENTA, uuid);
        }

        venta.setEstado(EstadoVenta.CANCELADA);
    }

    private BigDecimal registrarLineaSimple(Producto producto, LineaVentaRequestDto linea,
                                            String uuid, Usuario usuario, List<DetalleVenta> detalles) {
        List<MovimientoInventarioResponseDto> movimientos = inventarioService.consumirStockFEFO(
                producto.getId(), linea.getCantidad(), usuario.getId(), uuid);
        BigDecimal precioUnitario = producto.getPrecioBase();
        for (MovimientoInventarioResponseDto mov : movimientos) {
            BigDecimal subtotal = mov.getCantidad().multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);
            detalles.add(DetalleVenta.builder()
                    .producto(producto)
                    .lote(obtenerLote(mov.getLoteId()))
                    .cantidad(mov.getCantidad())
                    .precioUnitarioAplicado(precioUnitario)
                    .subtotal(subtotal)
                    .build());
        }
        return linea.getCantidad().multiply(precioUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal registrarLineaCompuesta(Producto producto, LineaVentaRequestDto linea,
                                               String uuid, Usuario usuario, List<DetalleVenta> detalles) {
        List<ProductoCompuesto> receta = productoCompuestoRepository.findByProductoPadreId(producto.getId());
        if (receta.isEmpty()) {
            throw new BusinessRuleException("El producto compuesto " + producto.getNombre() + " no tiene receta definida");
        }

        BigDecimal precioItem = receta.stream()
                .map(pc -> pc.getProductoHijo().getPrecioBase().multiply(pc.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal cantidadTotalConsumida = receta.stream()
                .map(pc -> pc.getCantidad().multiply(linea.getCantidad()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal subtotalItem = precioItem.multiply(linea.getCantidad()).setScale(2, RoundingMode.HALF_UP);

        for (ProductoCompuesto pc : receta) {
            BigDecimal cantidadHijo = pc.getCantidad().multiply(linea.getCantidad());
            List<MovimientoInventarioResponseDto> movimientos = inventarioService.consumirStockFEFO(
                    pc.getProductoHijo().getId(), cantidadHijo, usuario.getId(), uuid);
            for (MovimientoInventarioResponseDto mov : movimientos) {
                BigDecimal subtotalTrozo = subtotalItem.multiply(mov.getCantidad())
                        .divide(cantidadTotalConsumida, 2, RoundingMode.HALF_UP);
                BigDecimal precioUnitarioTrozo = subtotalTrozo.divide(mov.getCantidad(), 4, RoundingMode.HALF_UP);
                detalles.add(DetalleVenta.builder()
                        .producto(pc.getProductoHijo())
                        .lote(obtenerLote(mov.getLoteId()))
                        .cantidad(mov.getCantidad())
                        .precioUnitarioAplicado(precioUnitarioTrozo)
                        .subtotal(subtotalTrozo)
                        .build());
            }
        }
        return subtotalItem;
    }

    private Lote obtenerLote(Integer loteId) {
        return loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
    }

    private void validarClienteExiste(Integer clienteId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Clientes WHERE id = ?", Integer.class, clienteId);
        if (count == null || count == 0) {
            throw new ResourceNotFoundException("Cliente no encontrado con ID: " + clienteId);
        }
    }

    private Venta obtenerVenta(String uuid) {
        return ventaRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con UUID: " + uuid));
    }

    private Usuario obtenerUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
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

    private VentaResponseDto mapToResponseSinDetalle(Venta v) {
        return VentaResponseDto.builder()
                .uuid(v.getUuid())
                .turnoId(v.getTurno().getId())
                .usuarioId(v.getUsuario().getId())
                .usuarioUsername(v.getUsuario().getUsername())
                .clienteId(v.getClienteId())
                .fechaHora(v.getFechaHora())
                .metodoPago(v.getMetodoPago())
                .total(v.getTotal())
                .estado(v.getEstado())
                .sincronizado(v.getSincronizado())
                .detalle(List.of())
                .build();
    }

    private DetalleVentaResponseDto mapDetalleToResponse(DetalleVenta d) {
        Producto producto = d.getProducto();
        Lote lote = d.getLote();
        return DetalleVentaResponseDto.builder()
                .id(d.getId())
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .plu(producto.getPlu())
                .esCompuesto(producto.getEsCompuesto())
                .loteId(lote.getId())
                .codigoLote(lote.getCodigoLote())
                .cantidad(d.getCantidad())
                .precioUnitarioAplicado(d.getPrecioUnitarioAplicado())
                .subtotal(d.getSubtotal())
                .build();
    }
}