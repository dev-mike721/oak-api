package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.ImpuestoRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoImpuestoRequestDto;
import com.abarrotes.oakapi.dto.response.ImpuestoResponseDto;
import com.abarrotes.oakapi.dto.response.ProductoImpuestoResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Impuesto;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.ProductoImpuesto;
import com.abarrotes.oakapi.repository.ImpuestoRepository;
import com.abarrotes.oakapi.repository.DetalleVentaImpuestoRepository;
import com.abarrotes.oakapi.repository.ProductoImpuestoRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.ImpuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImpuestoServiceImpl implements ImpuestoService {

    private final ImpuestoRepository impuestoRepository;
    private final ProductoImpuestoRepository productoImpuestoRepository;
    private final DetalleVentaImpuestoRepository detalleVentaImpuestoRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<com.abarrotes.oakapi.dto.response.ImpuestoResponseDto> obtenerTodos() {
        return impuestoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public com.abarrotes.oakapi.dto.response.ImpuestoResponseDto obtenerPorId(Integer id) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado con ID: " + id));
        return mapToResponse(impuesto);
    }

    @Override
    @Transactional
    public com.abarrotes.oakapi.dto.response.ImpuestoResponseDto crear(ImpuestoRequestDto request) {
        if (impuestoRepository.existsByClave(request.getClave())) {
            throw new DuplicateResourceException("Ya existe un impuesto con la clave: " + request.getClave());
        }

        Impuesto impuesto = Impuesto.builder()
                .clave(request.getClave())
                .nombre(request.getNombre())
                .tasaTraslado(request.getTasaTraslado() != null ? request.getTasaTraslado() : BigDecimal.ZERO)
                .tasaRetencion(request.getTasaRetencion() != null ? request.getTasaRetencion() : BigDecimal.ZERO)
                .tipo(request.getTipo() != null ? request.getTipo() : com.abarrotes.oakapi.model.enums.TipoImpuesto.TRASLADO)
                .activo(request.getActivo() != null ? request.getActivo() : true)
                .build();

        return mapToResponse(impuestoRepository.save(impuesto));
    }

    @Override
    @Transactional
    public com.abarrotes.oakapi.dto.response.ImpuestoResponseDto actualizar(Integer id, ImpuestoRequestDto request) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado con ID: " + id));

        if (!impuesto.getClave().equals(request.getClave()) && impuestoRepository.existsByClave(request.getClave())) {
            throw new DuplicateResourceException("Ya existe otro impuesto con la clave: " + request.getClave());
        }

        impuesto.setClave(request.getClave());
        impuesto.setNombre(request.getNombre());
        impuesto.setTasaTraslado(request.getTasaTraslado() != null ? request.getTasaTraslado() : BigDecimal.ZERO);
        impuesto.setTasaRetencion(request.getTasaRetencion() != null ? request.getTasaRetencion() : BigDecimal.ZERO);
        impuesto.setTipo(request.getTipo() != null ? request.getTipo() : com.abarrotes.oakapi.model.enums.TipoImpuesto.TRASLADO);
        impuesto.setActivo(request.getActivo() != null ? request.getActivo() : true);

        return mapToResponse(impuesto);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Impuesto impuesto = impuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado con ID: " + id));

        // Verificar que no esté asignado a productos
        if (!productoImpuestoRepository.findByImpuestoId(id).isEmpty()) {
            throw new BusinessRuleException("No se puede eliminar el impuesto porque está asignado a productos");
        }

        // Verificar que no esté aplicado en ventas ya emitidas
        if (detalleVentaImpuestoRepository.existsByImpuestoId(id)) {
            throw new BusinessRuleException("No se puede eliminar el impuesto porque ya está aplicado en ventas");
        }

        impuestoRepository.delete(impuesto);
    }

    @Override
    @Transactional
    public com.abarrotes.oakapi.dto.response.ProductoImpuestoResponseDto asignarImpuestoAProducto(Integer productoId, com.abarrotes.oakapi.dto.request.ProductoImpuestoRequestDto request) {
        Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        Impuesto impuesto = impuestoRepository.findById(request.getImpuestoId())
                .orElseThrow(() -> new ResourceNotFoundException("Impuesto no encontrado con ID: " + request.getImpuestoId()));

        if (productoImpuestoRepository.existsByProductoIdAndImpuestoId(productoId, request.getImpuestoId())) {
            throw new BusinessRuleException("El impuesto ya está asignado a este producto");
        }

        ProductoImpuesto pi = ProductoImpuesto.builder()
                .producto(productoRepository.findById(productoId).orElseThrow())
                .impuesto(impuestoRepository.findById(request.getImpuestoId()).orElseThrow())
                .tasaOverride(request.getTasaOverride())
                .build();

        return mapToProductoImpuestoResponse(productoImpuestoRepository.save(pi));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.abarrotes.oakapi.dto.response.ProductoImpuestoResponseDto> obtenerImpuestosDeProducto(Integer productoId) {
        return productoImpuestoRepository.findByProductoId(productoId).stream()
                .map(this::mapToProductoImpuestoResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void quitarImpuestoDeProducto(Integer productoId, Integer impuestoId) {
        productoImpuestoRepository.deleteById(
                new com.abarrotes.oakapi.model.ProductoImpuestoId(productoId, impuestoId)
        );
    }

    private com.abarrotes.oakapi.dto.response.ImpuestoResponseDto mapToResponse(Impuesto i) {
        return com.abarrotes.oakapi.dto.response.ImpuestoResponseDto.builder()
                .id(i.getId())
                .clave(i.getClave())
                .nombre(i.getNombre())
                .tasaTraslado(i.getTasaTraslado())
                .tasaRetencion(i.getTasaRetencion())
                .tipo(i.getTipo())
                .activo(i.getActivo())
                .createdAt(i.getCreatedAt())
                .updatedAt(i.getUpdatedAt())
                .build();
    }

    private com.abarrotes.oakapi.dto.response.ProductoImpuestoResponseDto mapToProductoImpuestoResponse(ProductoImpuesto pi) {
        return com.abarrotes.oakapi.dto.response.ProductoImpuestoResponseDto.builder()
                .productoId(pi.getProducto().getId())
                .impuestoId(pi.getImpuesto().getId())
                .impuestoClave(pi.getImpuesto().getClave())
                .impuestoNombre(pi.getImpuesto().getNombre())
                .tasaTrasladoEfectiva(pi.getTasaOverride() != null ? pi.getTasaOverride() : pi.getImpuesto().getTasaTraslado())
                .tasaRetencionEfectiva(pi.getTasaOverride() != null ? pi.getTasaOverride() : pi.getImpuesto().getTasaRetencion())
                .tipo(pi.getImpuesto().getTipo())
                .tasaOverride(pi.getTasaOverride())
                .createdAt(pi.getCreatedAt())
                .build();
    }
}