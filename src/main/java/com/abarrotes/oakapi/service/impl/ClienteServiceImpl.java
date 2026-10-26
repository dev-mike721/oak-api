package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.ClientePrecioEspecialRequestDto;
import com.abarrotes.oakapi.dto.request.ClienteRequestDto;
import com.abarrotes.oakapi.dto.response.AgingClienteDto;
import com.abarrotes.oakapi.dto.response.AgingResponseDto;
import com.abarrotes.oakapi.dto.response.ClientePrecioEspecialResponseDto;
import com.abarrotes.oakapi.dto.response.ClienteResponseDto;
import com.abarrotes.oakapi.dto.response.CuentaCorrienteResponseDto;
import com.abarrotes.oakapi.dto.response.PagoResumenDto;
import com.abarrotes.oakapi.dto.response.VentaPendienteDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.ClientePrecioEspecial;
import com.abarrotes.oakapi.model.ListaPrecio;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.Venta;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.repository.ClientePrecioEspecialRepository;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.ListaPrecioRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.VentaRepository;
import com.abarrotes.oakapi.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClientePrecioEspecialRepository clientePrecioEspecialRepository;
    private final ListaPrecioRepository listaPrecioRepository;
    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final PrecioResolverServiceImpl precioResolverServiceImpl;

    @Override
    @Transactional(readOnly = true)
    public Page<ClienteResponseDto> obtenerTodos(Pageable pageable) {
        return clienteRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorId(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteResponseDto obtenerPorRfc(String rfc) {
        Cliente cliente = clienteRepository.findByRfc(rfc)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con RFC: " + rfc));
        return mapToResponse(cliente);
    }

    @Override
    @Transactional
    public ClienteResponseDto crear(ClienteRequestDto request) {
        if (clienteRepository.existsByRfc(request.getRfc())) {
            throw new DuplicateResourceException("Ya existe un cliente con el RFC: " + request.getRfc());
        }

        if (request.getEmail() != null && clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe un cliente con el email: " + request.getEmail());
        }

        ListaPrecio listaPrecio = null;
        if (request.getListaPrecioId() != null) {
            listaPrecio = listaPrecioRepository.findById(request.getListaPrecioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada con ID: " + request.getListaPrecioId()));
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.getNombre())
                .razonSocial(request.getRazonSocial())
                .rfc(request.getRfc().toUpperCase())
                .regimenFiscal(request.getRegimenFiscal())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .direccion(request.getDireccion())
                .limiteCredito(request.getLimiteCredito() != null ? request.getLimiteCredito() : BigDecimal.ZERO)
                .diasCredito(request.getDiasCredito() != null ? request.getDiasCredito() : 0)
                .listaPrecio(listaPrecio)
                .activo(true)
                .build();

        return mapToResponse(clienteRepository.save(cliente));
    }

    @Override
    @Transactional
    public ClienteResponseDto actualizar(Integer id, ClienteRequestDto request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (!cliente.getRfc().equals(request.getRfc()) && clienteRepository.existsByRfc(request.getRfc())) {
            throw new DuplicateResourceException("Ya existe otro cliente con el RFC: " + request.getRfc());
        }

        if (request.getEmail() != null && !request.getEmail().equals(cliente.getEmail()) && clienteRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Ya existe otro cliente con el email: " + request.getEmail());
        }

        ListaPrecio listaPrecio = null;
        if (request.getListaPrecioId() != null) {
            listaPrecio = listaPrecioRepository.findById(request.getListaPrecioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada con ID: " + request.getListaPrecioId()));
        }

        cliente.setNombre(request.getNombre());
        cliente.setRazonSocial(request.getRazonSocial());
        cliente.setRfc(request.getRfc().toUpperCase());
        cliente.setRegimenFiscal(request.getRegimenFiscal());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setLimiteCredito(request.getLimiteCredito() != null ? request.getLimiteCredito() : BigDecimal.ZERO);
        cliente.setDiasCredito(request.getDiasCredito() != null ? request.getDiasCredito() : 0);
        cliente.setListaPrecio(listaPrecio);

        return mapToResponse(cliente);
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (cliente.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException(
                    String.format("Cliente tiene saldo pendiente de %s. Registre pagos antes de desactivar", cliente.getSaldoPendiente())
            );
        }

        List<Venta> ventasPendientes = ventaRepository.findByClienteIdAndEstadoAndMetodoPago(
                id, EstadoVenta.COMPLETADA, MetodoPago.CREDITO
        );
        if (!ventasPendientes.isEmpty()) {
            throw new BusinessRuleException("Cliente tiene ventas a crédito pendientes. No se puede desactivar");
        }

        cliente.setActivo(false);
    }

    @Override
    @Transactional
    public void reactivar(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + id));

        if (Boolean.TRUE.equals(cliente.getActivo())) {
            throw new BusinessRuleException("El cliente ya se encuentra activo");
        }
        cliente.setActivo(true);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaCorrienteResponseDto obtenerCuentaCorriente(Integer clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + clienteId));

        List<Venta> ventasPendientes = ventaRepository.findByClienteIdAndEstadoAndMetodoPago(
                clienteId, EstadoVenta.COMPLETADA, MetodoPago.CREDITO
        ).stream()
                .filter(v -> v.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
                .toList();

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

        BigDecimal totalPendiente = ventasDto.stream()
                .map(VentaPendienteDto::getSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CuentaCorrienteResponseDto.builder()
                .clienteId(cliente.getId())
                .clienteNombre(cliente.getNombre())
                .saldoTotal(cliente.getSaldoPendiente())
                .ventasPendientes(ventasDto)
                .totalVentasPendientes(totalPendiente)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AgingResponseDto obtenerAging(LocalDate fechaCorte) {
        List<Cliente> clientesConDeuda = clienteRepository.findClientesConSaldoPendiente();

        List<AgingClienteDto> detalle = new ArrayList<>();
        BigDecimal totalCorriente = BigDecimal.ZERO;
        BigDecimal total31a60 = BigDecimal.ZERO;
        BigDecimal total61a90 = BigDecimal.ZERO;
        BigDecimal total91a120 = BigDecimal.ZERO;
        BigDecimal totalMas120 = BigDecimal.ZERO;

        for (Cliente cliente : clientesConDeuda) {
            List<Venta> ventas = ventaRepository.findByClienteIdAndEstadoAndMetodoPago(
                    cliente.getId(), EstadoVenta.COMPLETADA, MetodoPago.CREDITO
            ).stream()
                    .filter(v -> v.getSaldoPendiente().compareTo(BigDecimal.ZERO) > 0)
                    .toList();

            BigDecimal corriente = BigDecimal.ZERO;
            BigDecimal d31a60 = BigDecimal.ZERO;
            BigDecimal d61a90 = BigDecimal.ZERO;
            BigDecimal d91a120 = BigDecimal.ZERO;
            BigDecimal dMas120 = BigDecimal.ZERO;

            for (Venta venta : ventas) {
                long diasVencido = venta.getFechaVencimiento() != null ?
                        java.time.temporal.ChronoUnit.DAYS.between(venta.getFechaVencimiento(), fechaCorte) : 0;

                BigDecimal saldo = venta.getSaldoPendiente();
                if (diasVencido <= 0) {
                    corriente = corriente.add(saldo);
                } else if (diasVencido <= 30) {
                    corriente = corriente.add(saldo);
                } else if (diasVencido <= 60) {
                    d31a60 = d31a60.add(saldo);
                } else if (diasVencido <= 90) {
                    d61a90 = d61a90.add(saldo);
                } else if (diasVencido <= 120) {
                    d91a120 = d91a120.add(saldo);
                } else {
                    dMas120 = dMas120.add(saldo);
                }
            }

            BigDecimal total = corriente.add(d31a60).add(d61a90).add(d91a120).add(dMas120);

            detalle.add(AgingClienteDto.builder()
                    .clienteId(cliente.getId())
                    .clienteNombre(cliente.getNombre())
                    .clienteRfc(cliente.getRfc())
                    .corriente(corriente)
                    .dias31a60(d31a60)
                    .dias61a90(d61a90)
                    .dias91a120(d91a120)
                    .mas120(dMas120)
                    .total(total)
                    .build());

            totalCorriente = totalCorriente.add(corriente);
            total31a60 = total31a60.add(d31a60);
            total61a90 = total61a90.add(d61a90);
            total91a120 = total91a120.add(d91a120);
            totalMas120 = totalMas120.add(dMas120);
        }

        return AgingResponseDto.builder()
                .totalCorriente(totalCorriente)
                .total31a60(total31a60)
                .total61a90(total61a90)
                .total91a120(total91a120)
                .totalMas120(totalMas120)
                .totalGeneral(totalCorriente.add(total31a60).add(total61a90).add(total91a120).add(totalMas120))
                .clientesConDeuda(detalle.size())
                .detallePorCliente(detalle)
                .build();
    }

    @Override
    @Transactional
    public ClientePrecioEspecialResponseDto crearPrecioEspecial(Integer clienteId, ClientePrecioEspecialRequestDto request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + clienteId));

        // Validar solapamiento de vigencias
        List<ClientePrecioEspecial> vigentes = clientePrecioEspecialRepository
                .findOverlappingByClienteAndProducto(
                        clienteId, request.getProductoId(), request.getVigenciaInicio().atStartOfDay()
                );

        if (!vigentes.isEmpty()) {
            throw new BusinessRuleException(
                    "Ya existe un precio especial vigente para este cliente y producto en ese rango de fechas"
            );
        }

        ClientePrecioEspecial precioEspecial = ClientePrecioEspecial.builder()
                .cliente(cliente)
                .producto(productoRepository.findById(request.getProductoId()).orElseThrow(
                        () -> new ResourceNotFoundException("Producto no encontrado")
                ))
                .precio(request.getPrecio())
                .vigenciaInicio(request.getVigenciaInicio().atStartOfDay())
                .vigenciaFin(request.getVigenciaFin() != null ? request.getVigenciaFin().atStartOfDay() : null)
                .build();

        ClientePrecioEspecialResponseDto respuesta =
                mapToPrecioEspecialResponse(clientePrecioEspecialRepository.save(precioEspecial));

        // Sin esto el resolver seguiria devolviendo el precio anterior hasta 5 minutos.
        precioResolverServiceImpl.invalidarClienteProducto(clienteId, request.getProductoId());
        return respuesta;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientePrecioEspecialResponseDto> obtenerPreciosEspeciales(Integer clienteId) {
        clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con ID: " + clienteId));

        return clientePrecioEspecialRepository.findByClienteId(clienteId).stream()
                .map(this::mapToPrecioEspecialResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarPrecioEspecial(Integer clienteId, Integer productoId) {
        clientePrecioEspecialRepository.deleteById(
                new com.abarrotes.oakapi.model.ClientePrecioEspecialId(clienteId, productoId)
        );
        precioResolverServiceImpl.invalidarClienteProducto(clienteId, productoId);
    }

    private ClienteResponseDto mapToResponse(Cliente c) {
        return ClienteResponseDto.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .razonSocial(c.getRazonSocial())
                .rfc(c.getRfc())
                .regimenFiscal(c.getRegimenFiscal())
                .email(c.getEmail())
                .telefono(c.getTelefono())
                .direccion(c.getDireccion())
                .limiteCredito(c.getLimiteCredito())
                .diasCredito(c.getDiasCredito())
                .saldoPendiente(c.getSaldoPendiente())
                .listaPrecioId(c.getListaPrecio() != null ? c.getListaPrecio().getId() : null)
                .listaPrecioNombre(c.getListaPrecio() != null ? c.getListaPrecio().getNombre() : null)
                .activo(c.getActivo())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private ClientePrecioEspecialResponseDto mapToPrecioEspecialResponse(ClientePrecioEspecial pe) {
        return ClientePrecioEspecialResponseDto.builder()
                .clienteId(pe.getCliente().getId())
                .productoId(pe.getProducto().getId())
                .productoNombre(pe.getProducto().getNombre())
                .productoPlu(pe.getProducto().getPlu())
                .precio(pe.getPrecio())
                .vigenciaInicio(pe.getVigenciaInicio().toLocalDate())
                .vigenciaFin(pe.getVigenciaFin() != null ? pe.getVigenciaFin().toLocalDate() : null)
                .createdAt(pe.getCreatedAt())
                .build();
    }
}