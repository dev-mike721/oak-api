package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.CajaRequestDto;
import com.abarrotes.oakapi.dto.response.CajaResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Caja;
import com.abarrotes.oakapi.repository.CajaRepository;
import com.abarrotes.oakapi.service.CajaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CajaServiceImpl implements CajaService {

    private final CajaRepository cajaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CajaResponseDto> obtenerTodas() {
        return cajaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaResponseDto> obtenerActivas() {
        return cajaRepository.findByActivaTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CajaResponseDto obtenerPorId(Integer id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con ID: " + id));
        return mapToResponse(caja);
    }

    @Override
    @Transactional
    public CajaResponseDto crear(CajaRequestDto request) {
        if (cajaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe una caja con el nombre: " + request.getNombre());
        }

        Caja caja = Caja.builder()
                .nombre(request.getNombre())
                .ubicacion(request.getUbicacion())
                .activa(request.getActiva() != null ? request.getActiva() : true)
                .build();

        return mapToResponse(cajaRepository.save(caja));
    }

    @Override
    @Transactional
    public CajaResponseDto actualizar(Integer id, CajaRequestDto request) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con ID: " + id));

        if (!caja.getNombre().equals(request.getNombre()) && cajaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe otra caja con el nombre: " + request.getNombre());
        }

        caja.setNombre(request.getNombre());
        caja.setUbicacion(request.getUbicacion());
        caja.setActiva(request.getActiva() != null ? request.getActiva() : true);

        return mapToResponse(caja);
    }

    @Override
    @Transactional
    public void desactivar(Integer id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con ID: " + id));
        caja.setActiva(false);
    }

    @Override
    @Transactional
    public void activar(Integer id) {
        Caja caja = cajaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con ID: " + id));
        caja.setActiva(true);
    }

    private CajaResponseDto mapToResponse(Caja c) {
        return CajaResponseDto.builder()
                .id(c.getId())
                .nombre(c.getNombre())
                .ubicacion(c.getUbicacion())
                .activa(c.getActiva())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}