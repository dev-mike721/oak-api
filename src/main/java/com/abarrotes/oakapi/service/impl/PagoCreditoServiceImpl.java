package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto;
import com.abarrotes.oakapi.dto.response.CuentaCorrienteResponseDto;
import com.abarrotes.oakapi.dto.response.PagoCreditoAplicacionResponseDto;
import com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto;
import com.abarrotes.oakapi.dto.response.PagoResumenDto;
import com.abarrotes.oakapi.dto.response.VentaPendienteDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.PagoCredito;
import com.abarrotes.oakapi.model.PagoCreditoAplicacion;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.Venta;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.PagoCreditoAplicacionRepository;
import com.abarrotes.oakapi.repository.PagoCreditoRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.repository.VentaRepository;
import com.abarrotes.oakapi.service.AuditoriaService;
import com.abarrotes.oakapi.service.PagoCreditoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class PagoCreditoServiceImpl implements PagoCreditoService {

    private final PagoCreditoRepository pagoCreditoRepository;
    private final PagoCreditoAplicacionRepository pagoCreditoAplicacionRepository;
    private final AuditoriaService auditoriaService;
    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional
    public com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto registrarPago(PagoCreditoRequestDto request) {
        Cliente cliente = clienteRepository.findByIdAndActivoTrue(request.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + request.getClienteId()));

        PagoCredito pago = PagoCredito.builder()
                .cliente(cliente)
                .monto(request.getMonto())
                .fecha(LocalDateTime.now())
                .metodoPago(request.getMetodoPago())
                .referencia(request.getReferencia())
                .aplicado(false)
                .montoRestante(request.getMonto())
                .usuario(obtenerUsuarioActual())
                .build();

        return mapToResponse(pagoCreditoRepository.save(pago));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto> obtenerPorCliente(Integer clienteId) {
        return pagoCreditoRepository.findByClienteId(clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto obtenerPorId(Integer id) {
        PagoCredito pago = pagoCreditoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + id));
        return mapToResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.abarrotes.oakapi.dto.response.PagoCreditoAplicacionResponseDto> obtenerAplicaciones(Integer pagoId) {
        return pagoCreditoAplicacionRepository.findByPagoId(pagoId).stream()
                .map(this::mapToAplicacionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void aplicarFIFO(Integer pagoId) {
        PagoCredito pago = pagoCreditoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + pagoId));

        if (pago.getAnulado()) {
            throw new BusinessRuleException("No se puede aplicar un pago anulado");
        }
        if (pago.getAplicado()) {
            throw new BusinessRuleException("El pago ya fue aplicado");
        }
        if (pago.getMontoRestante().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("No hay monto restante por aplicar");
        }

        // Ventas pendientes ordenadas por FIFO (fecha_vencimiento ASC, fecha_hora ASC).
        // No se filtra por vencimiento: un pago anticipado debe liquidar la deuda mas
        // antigua aunque la factura aun no venza, igual que hace obtenerCuentaCorriente.
        List<Venta> ventasPendientes = ventaRepository
                .findByClienteIdAndEstadoAndMetodoPago(pago.getCliente().getId(), EstadoVenta.COMPLETADA, MetodoPago.CREDITO)
                .stream()
                .filter(v -> v.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator
                        .comparing(Venta::getFechaVencimiento, Comparator.nullsLast(LocalDate::compareTo))
                        .thenComparing(Venta::getFechaHora, Comparator.nullsLast(LocalDateTime::compareTo)))
                .collect(Collectors.toList());

        if (ventasPendientes.isEmpty()) {
            throw new BusinessRuleException("No se encontraron facturas pendientes para aplicar el pago");
        }

        BigDecimal montoDisponible = pago.getMontoRestante();
        for (Venta venta : ventasPendientes) {
            if (montoDisponible.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal saldoVenta = venta.getSaldoPendiente();
            BigDecimal montoAplicado = montoDisponible.min(saldoVenta);

            // Crear registro de aplicación
            PagoCreditoAplicacion aplicacion = PagoCreditoAplicacion.builder()
                    .pago(pago)
                    .venta(venta)
                    .montoAplicado(montoAplicado)
                    .fechaAplicacion(LocalDateTime.now())
                    .build();
            pagoCreditoAplicacionRepository.save(aplicacion);

            // Actualizar saldo de la venta
            venta.setSaldoPendiente(saldoVenta.subtract(montoAplicado));
            ventaRepository.save(venta);

            montoDisponible = montoDisponible.subtract(montoAplicado);
        }

        pago.setMontoRestante(montoDisponible);
        if (montoDisponible.compareTo(BigDecimal.ZERO) <= 0) {
            pago.setAplicado(true);
        }
        pagoCreditoRepository.save(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaCorrienteResponseDto obtenerCuentaCorriente(Integer clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + clienteId));

        List<com.abarrotes.oakapi.model.Venta> ventasPendientes = ventaRepository
                .findByClienteIdAndEstadoAndMetodoPago(clienteId, EstadoVenta.COMPLETADA, MetodoPago.CREDITO)
                .stream()
                .filter(v -> v.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        List<VentaPendienteDto> ventasDto = ventasPendientes.stream()
                .map(v -> VentaPendienteDto.builder()
                        .uuid(v.getUuid())
                        .fecha(v.getFechaHora())
                        .fechaVencimiento(v.getFechaVencimiento())
                        .total(v.getTotal())
                        .saldoPendiente(v.getSaldoPendiente())
                        .diasVencido(v.getFechaVencimiento() != null ?
                                (int) java.time.temporal.ChronoUnit.DAYS.between(v.getFechaVencimiento(), LocalDate.now()) : 0)
                        .build())
                .collect(Collectors.toList());

        List<PagoCreditoResponseDto> pagos = pagoCreditoRepository
                .findByClienteId(clienteId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        List<PagoResumenDto> pagosDto = pagos.stream()
                .map(p -> PagoResumenDto.builder()
                        .id(p.getId())
                        .fecha(p.getFecha())
                        .monto(p.getMonto())
                        .aplicado(p.getAplicado())
                        .montoRestante(p.getMontoRestante())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalVentasPendientes = ventasDto.stream()
                .map(VentaPendienteDto::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagosAplicados = pagos.stream()
                .filter(p -> p.getAplicado())
                .map(PagoCreditoResponseDto::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPagosPendientes = pagos.stream()
                .filter(p -> !p.getAplicado())
                .map(PagoCreditoResponseDto::getMontoRestante)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CuentaCorrienteResponseDto.builder()
                .clienteId(cliente.getId())
                .clienteNombre(cliente.getNombre())
                .saldoTotal(cliente.getSaldoPendiente())
                .ventasPendientes(ventasDto)
                .pagos(pagosDto)
                .totalVentasPendientes(totalVentasPendientes)
                .totalPagosAplicados(totalPagosAplicados)
                .totalPagosPendientes(totalPagosPendientes)
                .build();
    }

    @Override
    @Transactional
    public void anularPago(Integer pagoId) {
        PagoCredito pago = pagoCreditoRepository.findById(pagoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado con ID: " + pagoId));

        if (pago.getAnulado()) {
            throw new BusinessRuleException("El pago ya está anulado");
        }

        if (pago.getAplicado()) {
            List<com.abarrotes.oakapi.model.PagoCreditoAplicacion> aplicaciones = pagoCreditoAplicacionRepository.findByPagoId(pagoId);
            for (com.abarrotes.oakapi.model.PagoCreditoAplicacion app : aplicaciones) {
                Venta venta = ventaRepository.findById(app.getVenta().getUuid())
                        .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada: " + app.getVenta().getUuid()));
                venta.setSaldoPendiente(venta.getSaldoPendiente().add(app.getMontoAplicado()));
                pagoCreditoAplicacionRepository.delete(app);
            }
        }

        var anulador = obtenerUsuarioActual();
        pago.setAnulado(true);
        pago.setAnuladoPor(anulador);
        pago.setAnuladoEn(LocalDateTime.now());

        auditoriaService.registrar(anulador, "PAGO_CREDITO_ANULADO", Map.of(
                "pagoId", pagoId,
                "clienteId", pago.getCliente().getId(),
                "monto", pago.getMonto()));
    }

    private com.abarrotes.oakapi.model.Usuario obtenerUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    private com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto mapToResponse(PagoCredito p) {
        return com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto.builder()
                .id(p.getId())
                .clienteId(p.getCliente().getId())
                .clienteNombre(p.getCliente().getNombre())
                .monto(p.getMonto())
                .fecha(p.getFecha())
                .metodoPago(p.getMetodoPago())
                .referencia(p.getReferencia())
                .aplicado(p.getAplicado())
                .montoRestante(p.getMontoRestante())
                .usuarioId(p.getUsuario().getId())
                .usuarioUsername(p.getUsuario().getUsername())
                .anulado(p.getAnulado())
                .anuladoPorId(p.getAnuladoPor() != null ? p.getAnuladoPor().getId() : null)
                .anuladoPorUsername(p.getAnuladoPor() != null ? p.getAnuladoPor().getUsername() : null)
                .anuladoEn(p.getAnuladoEn())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    private com.abarrotes.oakapi.dto.response.PagoCreditoAplicacionResponseDto mapToAplicacionResponse(PagoCreditoAplicacion a) {
        return com.abarrotes.oakapi.dto.response.PagoCreditoAplicacionResponseDto.builder()
                .pagoId(a.getPago().getId())
                .ventaId(a.getVenta().getUuid())
                .montoAplicado(a.getMontoAplicado())
                .fechaAplicacion(a.getFechaAplicacion())
                .build();
    }
}