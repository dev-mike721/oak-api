package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.ProveedorRequestDto;
import com.abarrotes.oakapi.dto.response.ProveedorResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Proveedor;
import com.abarrotes.oakapi.repository.ProveedorRepository;
import com.abarrotes.oakapi.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProveedorResponseDto> obtenerTodos(String termino, Pageable pageable) {
        if (StringUtils.hasText(termino)) {
            return proveedorRepository.findByActivoTrueAndRazonSocialContainingIgnoreCase(termino.trim(), pageable)
                    .map(this::mapToResponse);
        }
        return proveedorRepository.findByActivoTrue(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProveedorResponseDto buscarPorId(Integer id) {
        return mapToResponse(obtenerProveedorActivo(id));
    }

    @Override
    @Transactional
    public ProveedorResponseDto crear(ProveedorRequestDto request) {
        String rfc = normalizarRfc(request.getRfc());
        if (rfc != null && proveedorRepository.existsByRfc(rfc)) {
            throw new DuplicateResourceException("Ya existe un proveedor con el RFC: " + rfc);
        }

        Proveedor proveedor = Proveedor.builder()
                .razonSocial(request.getRazonSocial().trim())
                .rfc(rfc)
                .telefono(request.getTelefono())
                .email(request.getEmail())
                .direccion(request.getDireccion())
                .diasCredito(request.getDiasCredito())
                .activo(true)
                .build();

        return mapToResponse(proveedorRepository.save(proveedor));
    }

    @Override
    @Transactional
    public ProveedorResponseDto actualizar(Integer id, ProveedorRequestDto request) {
        Proveedor proveedor = obtenerProveedorActivo(id);

        String rfc = normalizarRfc(request.getRfc());
        if (rfc != null && proveedorRepository.existsByRfcAndIdNot(rfc, id)) {
            throw new DuplicateResourceException("Ya existe otro proveedor con el RFC: " + rfc);
        }

        proveedor.setRazonSocial(request.getRazonSocial().trim());
        proveedor.setRfc(rfc);
        proveedor.setTelefono(request.getTelefono());
        proveedor.setEmail(request.getEmail());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setDiasCredito(request.getDiasCredito());

        return mapToResponse(proveedor);
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Proveedor proveedor = obtenerProveedorActivo(id);
        proveedor.setActivo(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProveedorResponseDto> obtenerInactivos() {
        return proveedorRepository.findByActivoFalse().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProveedorResponseDto reactivar(Integer id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
        if (Boolean.TRUE.equals(proveedor.getActivo())) {
            throw new BusinessRuleException("El proveedor ya se encuentra activo");
        }
        proveedor.setActivo(true);
        return mapToResponse(proveedor);
    }

    private Proveedor obtenerProveedorActivo(Integer id) {
        return proveedorRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado con ID: " + id));
    }

    private String normalizarRfc(String rfc) {
        if (!StringUtils.hasText(rfc)) {
            return null;
        }
        return rfc.trim().toUpperCase();
    }

    private ProveedorResponseDto mapToResponse(Proveedor p) {
        return ProveedorResponseDto.builder()
                .id(p.getId())
                .razonSocial(p.getRazonSocial())
                .rfc(p.getRfc())
                .telefono(p.getTelefono())
                .email(p.getEmail())
                .direccion(p.getDireccion())
                .diasCredito(p.getDiasCredito())
                .activo(p.getActivo())
                .fechaRegistro(p.getFechaRegistro())
                .build();
    }
}