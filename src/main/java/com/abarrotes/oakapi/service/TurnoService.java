package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.AperturaTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.CierreTurnoRequestDto;
import com.abarrotes.oakapi.dto.response.TurnoResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TurnoService {

    Page<TurnoResponseDto> obtenerTodas(EstadoTurno estado, Integer usuarioId,
                                        LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    TurnoResponseDto obtenerPorId(Integer id);

    TurnoResponseDto obtenerActual();

    TurnoResponseDto abrir(AperturaTurnoRequestDto request);

    TurnoResponseDto cerrar(Integer id, CierreTurnoRequestDto request);
}