package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.ConvertirPresupuestoRequestDto;
import com.abarrotes.oakapi.dto.request.PresupuestoDetalleRequestDto;
import com.abarrotes.oakapi.dto.request.PresupuestoRequestDto;
import com.abarrotes.oakapi.dto.response.PresupuestoDetalleResponseDto;
import com.abarrotes.oakapi.dto.response.PresupuestoResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.Impuesto;
import com.abarrotes.oakapi.model.DetalleVentaImpuesto;
import com.abarrotes.oakapi.model.Presupuesto;
import com.abarrotes.oakapi.model.PresupuestoDetalle;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.Venta;
import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.ImpuestoRepository;
import com.abarrotes.oakapi.repository.LoteRepository;
import com.abarrotes.oakapi.repository.PresupuestoDetalleRepository;
import com.abarrotes.oakapi.repository.PresupuestoRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.repository.VentaRepository;
import com.abarrotes.oakapi.service.AuditoriaService;
import com.abarrotes.oakapi.service.InventarioService;
import com.abarrotes.oakapi.service.PrecioResolverService;
import com.abarrotes.oakapi.service.PresupuestoService;
import com.abarrotes.oakapi.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresupuestoServiceImpl implements PresupuestoService {

    private final PresupuestoRepository presupuestoRepository;
    private final AuditoriaService auditoriaService;
    private final PresupuestoDetalleRepository presupuestoDetalleRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final ImpuestoRepository impuestoRepository;
    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;
    private final PrecioResolverService precioResolverService;
    private final InventarioService inventarioService;
    private final LoteRepository loteRepository;

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<PresupuestoResponseDto> obtenerTodos(EstadoPresupuesto estado, Integer clienteId, Integer usuarioId,
                                                                                   LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        return presupuestoRepository.buscarConFiltros(estado, clienteId, usuarioId, fechaDesde, fechaHasta, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public PresupuestoResponseDto obtenerPorId(String id) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));
        return mapToResponse(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoResponseDto crear(PresupuestoRequestDto request) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.getClienteId()));

        Usuario usuario = obtenerUsuarioActual();

        List<PresupuestoDetalle> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal impuestosTotal = BigDecimal.ZERO;

        for (PresupuestoDetalleRequestDto linea : request.getLineas()) {
            Producto producto = productoRepository.findByIdAndActivoTrue(linea.getProductoId())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + linea.getProductoId()));

            // Resolver precio usando el servicio de precios
            com.abarrotes.oakapi.dto.response.PrecioResueltoResponseDto precioResuelto = precioResolverService.resolvePrecio(
                    cliente.getId(), producto.getId(), linea.getCantidad(), LocalDate.now()
            );

            // Calcular impuestos del producto
            BigDecimal precioUnitario = precioResuelto.getPrecioUnitario();
            BigDecimal subtotalLinea = precioUnitario.multiply(linea.getCantidad()).setScale(2, RoundingMode.HALF_UP);

            Impuesto impuesto = null;
            BigDecimal impuestoTasa = BigDecimal.ZERO;
            BigDecimal impuestoImporte = BigDecimal.ZERO;

            if (linea.getImpuestoId() != null) {
                impuesto = impuestoRepository.findById(linea.getImpuestoId())
                        .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado con ID: " + linea.getImpuestoId()));
                impuestoTasa = impuesto.getTasaTraslado();
                impuestoImporte = subtotalLinea.multiply(impuestoTasa).setScale(2, RoundingMode.HALF_UP);
            }

            PresupuestoDetalle detalle = PresupuestoDetalle.builder()
                    .producto(producto)
                    .cantidad(linea.getCantidad())
                    .precioUnitario(precioUnitario)
                    .impuesto(impuesto)
                    .impuestoTasa(impuestoTasa)
                    .impuestoImporte(impuestoImporte)
                    .subtotal(subtotalLinea)
                    .total(subtotalLinea.add(impuestoImporte))
                    .build();

            detalles.add(detalle);
            subtotal = subtotal.add(subtotalLinea);
            impuestosTotal = impuestosTotal.add(impuestoImporte);
        }

        LocalDate fechaVigencia = LocalDate.now().plusDays(request.getVigenciaDias());

        Presupuesto presupuesto = Presupuesto.builder()
                .id(UUID.randomUUID().toString())
                .cliente(cliente)
                .usuario(usuario)
                .fecha(LocalDate.now())
                .vigenciaDias(request.getVigenciaDias())
                .estado(EstadoPresupuesto.BORRADOR)
                .subtotal(subtotal)
                .impuestosTotal(impuestosTotal)
                .total(subtotal.add(impuestosTotal))
                .observaciones(request.getObservaciones())
                .detalles(detalles)
                .build();

        detalles.forEach(d -> d.setPresupuesto(presupuesto));
        presupuestoRepository.save(presupuesto);
        presupuestoDetalleRepository.saveAll(detalles);

        return mapToResponse(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoResponseDto enviar(String id) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));

        if (presupuesto.getEstado() != EstadoPresupuesto.BORRADOR) {
            throw new BusinessRuleException("Solo se pueden enviar presupuestos en estado BORRADOR");
        }

        presupuesto.setEstado(EstadoPresupuesto.ENVIADO);
        return mapToResponse(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoResponseDto aprobar(String id) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));

        if (presupuesto.getEstado() != EstadoPresupuesto.ENVIADO && presupuesto.getEstado() != EstadoPresupuesto.BORRADOR) {
            throw new BusinessRuleException("Solo se pueden aprobar presupuestos en estado ENVIADO o BORRADOR");
        }

        if (presupuesto.getFechaVigencia().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("No se puede aprobar un presupuesto vencido");
        }

        Usuario usuario = obtenerUsuarioActual();
        EstadoPresupuesto estadoPrevio = presupuesto.getEstado();
        presupuesto.setEstado(EstadoPresupuesto.APROBADO);
        presupuesto.setAprobadoPor(usuario);
        presupuesto.setAprobadoEn(LocalDateTime.now());

        // Aprobar es la accion mas sensible del ciclo del presupuesto: queda traza
        // de quien lo autorizo y desde que estado.
        auditoriaService.registrar(usuario, "PRESUPUESTO_APROBADO", Map.of(
                "presupuestoId", presupuesto.getId(),
                "clienteId", presupuesto.getCliente().getId(),
                "total", presupuesto.getTotal(),
                "estadoPrevio", estadoPrevio.name()));

        return mapToResponse(presupuesto);
    }

    @Override
    @Transactional
    public PresupuestoResponseDto convertirAVenta(String id, ConvertirPresupuestoRequestDto request) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));

        if (presupuesto.getEstado() != EstadoPresupuesto.APROBADO) {
            throw new BusinessRuleException("El presupuesto debe estar APROBADO para convertir a venta");
        }

        if (presupuesto.getFechaVigencia().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Presupuesto vencido, no se puede convertir");
        }

        // Validar cliente para crédito
        if (request.getMetodoPago() == MetodoPago.CREDITO) {
            if (request.getClienteId() == null && presupuesto.getCliente() == null) {
                throw new BusinessRuleException("El método de pago CREDITO requiere un cliente");
            }
        }

        // Usar cliente del presupuesto si no se especifica uno
        Cliente cliente = presupuesto.getCliente();
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findByIdAndActivoTrue(request.getClienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.getClienteId()));
        }

        Usuario usuario = obtenerUsuarioActual();

        TurnoCaja turno = obtenerTurnoActual(usuario);
        boolean esCredito = request.getMetodoPago() == MetodoPago.CREDITO;

        // Crear venta copiando datos del presupuesto
        Venta venta = Venta.builder()
                .uuid(UUID.randomUUID().toString())
                .turno(turno)
                .caja(turno.getCaja())
                .usuario(usuario)
                .cliente(cliente)
                .fechaHora(LocalDateTime.now())
                .metodoPago(request.getMetodoPago())
                .total(presupuesto.getTotal())
                .estado(EstadoVenta.COMPLETADA)
                .saldoPendiente(esCredito ? presupuesto.getTotal() : BigDecimal.ZERO)
                .fechaVencimiento(esCredito && cliente != null
                        ? LocalDate.now().plusDays(cliente.getDiasCredito()) : null)
                .presupuestoId(presupuesto.getId())
                .build();

        // Copiar líneas del presupuesto a la venta
        List<com.abarrotes.oakapi.model.DetalleVenta> detallesVenta = new ArrayList<>();
        for (PresupuestoDetalle pd : presupuesto.getDetalles()) {
            // Consumir stock usando FEFO para obtener lote(s)
            List<com.abarrotes.oakapi.dto.response.MovimientoInventarioResponseDto> movimientos = inventarioService.consumirStockFEFO(
                    pd.getProducto().getId(), pd.getCantidad(), usuario.getId(), venta.getUuid());
            
            for (com.abarrotes.oakapi.dto.response.MovimientoInventarioResponseDto mov : movimientos) {
                BigDecimal cantidadLote = mov.getCantidad();
                BigDecimal subtotalLote = pd.getPrecioUnitario().multiply(cantidadLote).setScale(2, RoundingMode.HALF_UP);
                
                com.abarrotes.oakapi.model.DetalleVenta dv = com.abarrotes.oakapi.model.DetalleVenta.builder()
                        .producto(pd.getProducto())
                        .lote(loteRepository.findById(mov.getLoteId()).orElseThrow())
                        .cantidad(cantidadLote)
                        .precioUnitarioAplicado(pd.getPrecioUnitario())
                        .subtotal(subtotalLote)
                        .impuestos(impuestosPresupuesto(pd, cantidadLote))
                        .build();
                detallesVenta.add(dv);
            }
        }

        detallesVenta.forEach(d -> d.setVenta(venta));
        venta.setDetalle(detallesVenta);
        ventaRepository.save(venta);

        // El presupuesto a crédito genera deuda para el cliente, igual que una venta
        if (esCredito && cliente != null) {
            cliente.setSaldoPendiente(cliente.getSaldoPendiente().add(presupuesto.getTotal()));
        }

        // Actualizar presupuesto
        presupuesto.setEstado(EstadoPresupuesto.CONVERTIDO);
        presupuesto.setVentaUuid(venta.getUuid());

        auditoriaService.registrar(usuario, "PRESUPUESTO_CONVERTIDO", Map.of(
                "presupuestoId", presupuesto.getId(),
                "ventaUuid", venta.getUuid(),
                "total", presupuesto.getTotal(),
                "metodoPago", request.getMetodoPago().name()));

        return mapToResponse(presupuesto);
    }

/** Reconstruye los impuestos del presupuesto como detalle de venta, repartidos por cantidad de lote. */
    private List<DetalleVentaImpuesto> impuestosPresupuesto(PresupuestoDetalle pd, BigDecimal cantidadLote) {
        List<DetalleVentaImpuesto> lista = new ArrayList<>();
        if (pd.getImpuesto() == null) {
            return lista;
        }
        BigDecimal tasa = pd.getImpuestoTasa() != null ? pd.getImpuestoTasa() : BigDecimal.ZERO;
        if (tasa.compareTo(BigDecimal.ZERO) <= 0) {
            return lista;
        }

        BigDecimal base = pd.getPrecioUnitario().multiply(cantidadLote).setScale(2, RoundingMode.HALF_UP);
        BigDecimal importe = pd.getImpuestoImporte() != null && pd.getImpuestoImporte().compareTo(BigDecimal.ZERO) > 0
                ? pd.getImpuestoImporte().multiply(cantidadLote)
                        .divide(pd.getCantidad(), 6, RoundingMode.HALF_UP)
                        .setScale(2, RoundingMode.HALF_UP)
                : base.multiply(tasa).setScale(2, RoundingMode.HALF_UP);

        lista.add(DetalleVentaImpuesto.builder()
                .impuesto(pd.getImpuesto())
                .base(base)
                .tasa(tasa)
                .importeTraslado(importe)
                .importeRetencion(BigDecimal.ZERO)
                .build());
        return lista;
    }

    @Override
    @Transactional
    public PresupuestoResponseDto cancelar(String id) {
        Presupuesto presupuesto = presupuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + id));
        EstadoPresupuesto estadoPrevio = presupuesto.getEstado();

        if (presupuesto.getEstado() == EstadoPresupuesto.CONVERTIDO) {
            throw new BusinessRuleException("No se puede cancelar un presupuesto ya convertido a venta");
        }

        presupuesto.setEstado(EstadoPresupuesto.CANCELADO);
        auditoriaService.registrar("PRESUPUESTO_CANCELADO", Map.of(
                "presupuestoId", presupuesto.getId(),
                "estadoPrevio", estadoPrevio));
        return mapToResponse(presupuesto);
    }

    @Override
    @Transactional
    public void vencerPresupuestos() {
        LocalDate hoy = LocalDate.now();

        // fechaVigencia es un getter calculado (fecha + vigenciaDias), no una columna,
        // así que el filtro se hace en memoria.
        List<Presupuesto> vencidos = presupuestoRepository.findByEstadoInPresupuesto(
                        List.of(EstadoPresupuesto.ENVIADO, EstadoPresupuesto.APROBADO))
                .stream()
                .filter(p -> p.getFechaVigencia() != null && p.getFechaVigencia().isBefore(hoy))
                .collect(Collectors.toList());

        for (Presupuesto p : vencidos) {
            p.setEstado(EstadoPresupuesto.VENCIDO);
        }
    }

    private PresupuestoResponseDto mapToResponse(Presupuesto p) {
        return PresupuestoResponseDto.builder()
                .id(p.getId())
                .clienteId(p.getCliente().getId())
                .clienteNombre(p.getCliente().getNombre())
                .usuarioId(p.getUsuario().getId())
                .usuarioUsername(p.getUsuario().getUsername())
                .fecha(p.getFecha())
                .vigenciaDias(p.getVigenciaDias())
                .estado(p.getEstado())
                .subtotal(p.getSubtotal())
                .impuestosTotal(p.getImpuestosTotal())
                .total(p.getTotal())
                .observaciones(p.getObservaciones())
                .aprobadoPor(p.getAprobadoPor() != null ? p.getAprobadoPor().getId() : null)
                .aprobadoPorUsername(p.getAprobadoPor() != null ? p.getAprobadoPor().getUsername() : null)
                .aprobadoEn(p.getAprobadoEn())
                .ventaUuid(p.getVentaUuid())
                .lineas(p.getDetalles().stream().map(this::mapDetalleToResponse).collect(Collectors.toList()))
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private PresupuestoDetalleResponseDto mapDetalleToResponse(PresupuestoDetalle d) {
        return PresupuestoDetalleResponseDto.builder()
                .id(d.getId())
                .presupuestoId(d.getPresupuesto().getId())
                .productoId(d.getProducto().getId())
                .productoNombre(d.getProducto().getNombre())
                .productoPlu(d.getProducto().getPlu())
                .cantidad(d.getCantidad())
                .precioUnitario(d.getPrecioUnitario())
                .impuestoId(d.getImpuesto() != null ? d.getImpuesto().getId() : null)
                .impuestoClave(d.getImpuesto() != null ? d.getImpuesto().getClave() : null)
                .impuestoTasa(d.getImpuestoTasa())
                .impuestoImporte(d.getImpuestoImporte())
                .subtotal(d.getSubtotal())
                .total(d.getTotal())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private Usuario obtenerUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    @Autowired
    private TurnoService turnoService;

    private com.abarrotes.oakapi.model.TurnoCaja obtenerTurnoActual(Usuario usuario) {
        // Obtener turno abierto del usuario
        return turnoService.obtenerTurnoAbiertoDelUsuario(usuario);
    }
}