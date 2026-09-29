package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.ProveedorRequestDto;
import com.abarrotes.oakapi.dto.response.ProveedorResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProveedorService {

    Page<ProveedorResponseDto> obtenerTodos(String termino, Pageable pageable);

    ProveedorResponseDto buscarPorId(Integer id);

    ProveedorResponseDto crear(ProveedorRequestDto request);

    ProveedorResponseDto actualizar(Integer id, ProveedorRequestDto request);

    void eliminar(Integer id);

    List<ProveedorResponseDto> obtenerInactivos();

    ProveedorResponseDto reactivar(Integer id);
}
