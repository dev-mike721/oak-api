package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.CompraRequestDto;
import com.abarrotes.oakapi.dto.response.CompraResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface CompraService {

    Page<CompraResponseDto> obtenerTodas(EstadoCompra estado, Integer proveedorId,
                                         LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    CompraResponseDto obtenerPorId(Integer id);

    CompraResponseDto crear(CompraRequestDto request);

    void pagar(Integer id);

    void cancelar(Integer id);
}