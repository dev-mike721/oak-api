package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.VentaRequestDto;
import com.abarrotes.oakapi.dto.response.VentaResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface VentaService {

    Page<VentaResponseDto> obtenerTodas(MetodoPago metodoPago, EstadoVenta estado, Integer usuarioId,
                                        Integer turnoId, LocalDate fechaDesde, LocalDate fechaHasta,
                                        Pageable pageable);

    VentaResponseDto obtenerPorId(String uuid);

    VentaResponseDto registrar(VentaRequestDto request);

    void cancelar(String uuid);
}