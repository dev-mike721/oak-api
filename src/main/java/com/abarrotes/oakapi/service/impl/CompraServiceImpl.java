package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.CompraRequestDto;
import com.abarrotes.oakapi.dto.request.DetalleCompraRequestDto;
import com.abarrotes.oakapi.dto.response.CompraResponseDto;
import com.abarrotes.oakapi.dto.response.DetalleCompraResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.*;
import com.abarrotes.oakapi.model.enums.DocumentoOrigen;
import com.abarrotes.oakapi.model.enums.EstadoCompra;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import com.abarrotes.oakapi.repository.*;
import com.abarrotes.oakapi.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompraServiceImpl implements CompraService {

    private static final String PREFIJO_LOTE = "LOT-";
    private static final DateTimeFormatter FORMATO_FECHA_LOTE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final LoteRepository loteRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<CompraResponseDto> obtenerTodas(EstadoCompra estado, Integer proveedorId,
                                                LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        LocalDateTime desde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = fechaHasta != null ? fechaHasta.plusDays(1).atStartOfDay() : null;
        return compraRepository.buscarConFiltros(estado, proveedorId, desde, hasta, pageable)
                .map(this::mapToResponseSinDetalle);
    }

    @Override
    @Transactional(readOnly = true)
    public CompraResponseDto obtenerPorId(Integer id) {
        Compra compra = obtenerCompra(id);
        CompraResponseDto dto = mapToResponseSinDetalle(compra);
        dto.setDetalle(detalleCompraRepository.findByCompraIdOrderByIdAsc(id).stream()
                .map(this::mapDetalleToResponse)
                .toList());
        return dto;
    }

    @Override
    @Transactional
    public CompraResponseDto crear(CompraRequestDto request) {
        Proveedor proveedor = proveedorRepository.findByIdAndActivoTrue(request.getProveedorId())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + request.getProveedorId()));
        Usuario usuario = obtenerUsuarioActual();

        if (StringUtils.hasText(request.getFolioFactura())
                && compraRepository.existsByFolioFactura(request.getFolioFactura().trim())) {
            throw new DuplicateResourceException("Ya existe una compra con el folio de factura: " + request.getFolioFactura());
        }

        LocalDate vencimiento = resolverVencimiento(request.getFechaVencimientoPago(), proveedor);
        BigDecimal total = request.getDetalle().stream()
                .map(this::calcularSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Compra compra = compraRepository.save(Compra.builder()
                .proveedor(proveedor)
                .usuario(usuario)
                .folioFactura(StringUtils.hasText(request.getFolioFactura()) ? request.getFolioFactura().trim() : null)
                .fechaCompra(LocalDateTime.now())
                .fechaVencimientoPago(vencimiento)
                .total(total)
                .estado(EstadoCompra.PENDIENTE_PAGO)
                .build());

        for (DetalleCompraRequestDto linea : request.getDetalle()) {
            Producto producto = productoRepository.findByIdAndActivoTrue(linea.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Producto no encontrado con ID: " + linea.getProductoId()));

            Lote lote = registrarLote(producto, linea, compra.getId(), usuario);
            detalleCompraRepository.save(DetalleCompra.builder()
                    .compra(compra)
                    .producto(producto)
                    .lote(lote)
                    .cantidad(linea.getCantidad())
                    .costoUnitario(linea.getCostoUnitario())
                    .subtotal(calcularSubtotal(linea))
                    .build());
        }

        return obtenerPorId(compra.getId());
    }

    @Override
    @Transactional
    public void pagar(Integer id) {
        Compra compra = obtenerCompra(id);
        if (compra.getEstado() == EstadoCompra.CANCELADA) {
            throw new BusinessRuleException("No se puede pagar una compra cancelada");
        }
        if (compra.getEstado() == EstadoCompra.PAGADA) {
            throw new BusinessRuleException("La compra ya está marcada como pagada");
        }
        compra.setEstado(EstadoCompra.PAGADA);
    }

    @Override
    @Transactional
    public void cancelar(Integer id) {
        Compra compra = obtenerCompra(id);
        if (compra.getEstado() == EstadoCompra.CANCELADA) {
            throw new BusinessRuleException("La compra ya está cancelada");
        }

        Usuario usuario = obtenerUsuarioActual();
        for (DetalleCompra detalle : detalleCompraRepository.findByCompraIdOrderByIdAsc(id)) {
            Lote lote = loteRepository.findByIdConLock(detalle.getLote().getId());
            if (lote == null) {
                throw new ResourceNotFoundException("Lote no encontrado con ID: " + detalle.getLote().getId());
            }
            BigDecimal anterior = lote.getCantidadActual();
            if (anterior.compareTo(detalle.getCantidad()) < 0) {
                throw new BusinessRuleException(String.format(
                        "No se puede cancelar: el lote %s ya no tiene el stock de la compra (disponible: %s, comprado: %s)",
                        lote.getCodigoLote(), anterior, detalle.getCantidad()));
            }
            BigDecimal nueva = anterior.subtract(detalle.getCantidad());
            lote.setCantidadActual(nueva);
            guardarMovimiento(lote, usuario, TipoMovimientoInventario.AJUSTE_NEGATIVO,
                    detalle.getCantidad(), anterior, nueva,
                    "Cancelación de compra #" + id, DocumentoOrigen.COMPRA, id.longValue());
        }

        compra.setEstado(EstadoCompra.CANCELADA);
    }

    private Lote registrarLote(Producto producto, DetalleCompraRequestDto linea, Integer compraId, Usuario usuario) {
        String codigoLote = StringUtils.hasText(linea.getCodigoLote())
                ? linea.getCodigoLote().trim().toUpperCase()
                : generarCodigoLote(producto.getId());

        if (loteRepository.existsByProductoIdAndCodigoLote(producto.getId(), codigoLote)) {
            throw new DuplicateResourceException(String.format(
                    "Ya existe un lote con el código %s para el producto %s", codigoLote, producto.getPlu()));
        }

        Lote lote = loteRepository.save(Lote.builder()
                .producto(producto)
                .codigoLote(codigoLote)
                .costoCompra(linea.getCostoUnitario())
                .cantidadInicial(linea.getCantidad())
                .cantidadActual(linea.getCantidad())
                .fechaRecepcion(LocalDate.now())
                .fechaCaducidad(linea.getFechaCaducidad())
                .build());

        guardarMovimiento(lote, usuario, TipoMovimientoInventario.ENTRADA_COMPRA,
                linea.getCantidad(), BigDecimal.ZERO, linea.getCantidad(),
                "Compra #" + compraId + " - Lote " + codigoLote, DocumentoOrigen.COMPRA, compraId.longValue());
        return lote;
    }

    private String generarCodigoLote(Integer productoId) {
        String prefijo = PREFIJO_LOTE + LocalDate.now().format(FORMATO_FECHA_LOTE) + "-";
        Optional<Lote> ultimo = loteRepository
                .findTopByProductoIdAndCodigoLoteStartingWithOrderByCodigoLoteDesc(productoId, prefijo);
        int siguiente = ultimo
                .map(l -> Integer.parseInt(l.getCodigoLote().substring(prefijo.length())) + 1)
                .orElse(1);
        return prefijo + String.format("%04d", siguiente);
    }

    private LocalDate resolverVencimiento(LocalDate solicitado, Proveedor proveedor) {
        if (solicitado != null) {
            return solicitado;
        }
        if (proveedor.getDiasCredito() != null && proveedor.getDiasCredito() > 0) {
            return LocalDate.now().plusDays(proveedor.getDiasCredito());
        }
        return null;
    }

    private BigDecimal calcularSubtotal(DetalleCompraRequestDto linea) {
        return linea.getCantidad().multiply(linea.getCostoUnitario()).setScale(2, RoundingMode.HALF_UP);
    }

    private Compra obtenerCompra(Integer id) {
        return compraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Compra no encontrada con ID: " + id));
    }

    private Usuario obtenerUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    private MovimientoInventario guardarMovimiento(Lote lote, Usuario usuario, TipoMovimientoInventario tipo,
                                                   BigDecimal cantidad, BigDecimal anterior, BigDecimal nueva,
                                                   String motivo, DocumentoOrigen documentoTipo, Long documentoId) {
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

    private CompraResponseDto mapToResponseSinDetalle(Compra c) {
        return CompraResponseDto.builder()
                .id(c.getId())
                .proveedorId(c.getProveedor().getId())
                .proveedorRazonSocial(c.getProveedor().getRazonSocial())
                .usuarioId(c.getUsuario().getId())
                .usuarioUsername(c.getUsuario().getUsername())
                .folioFactura(c.getFolioFactura())
                .fechaCompra(c.getFechaCompra())
                .fechaVencimientoPago(c.getFechaVencimientoPago())
                .total(c.getTotal())
                .estado(c.getEstado())
                .detalle(List.of())
                .build();
    }

    private DetalleCompraResponseDto mapDetalleToResponse(DetalleCompra d) {
        Producto producto = d.getProducto();
        Lote lote = d.getLote();
        return DetalleCompraResponseDto.builder()
                .id(d.getId())
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .plu(producto.getPlu())
                .loteId(lote.getId())
                .codigoLote(lote.getCodigoLote())
                .cantidad(d.getCantidad())
                .costoUnitario(d.getCostoUnitario())
                .subtotal(d.getSubtotal())
                .fechaCaducidad(lote.getFechaCaducidad())
                .build();
    }
}