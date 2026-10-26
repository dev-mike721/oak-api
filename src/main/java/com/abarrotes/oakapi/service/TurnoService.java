package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.AperturaTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.CierreTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.SolicitarTurnoRequestDto;
import com.abarrotes.oakapi.dto.response.TurnoResponseDto;
import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.EstadoSolicitudTurno;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface TurnoService {

    Page<TurnoResponseDto> obtenerTodas(EstadoTurno estado, EstadoSolicitudTurno estadoSolicitud, Integer cajaId, Integer usuarioId,
                                        LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    TurnoResponseDto obtenerPorId(Integer id);

    TurnoResponseDto obtenerActual();

    TurnoResponseDto solicitarApertura(SolicitarTurnoRequestDto request);

    TurnoResponseDto autorizar(Integer turnoId);

    TurnoResponseDto rechazar(Integer turnoId, String motivo);

    TurnoResponseDto cerrar(Integer id, CierreTurnoRequestDto request);

    TurnoResponseDto obtenerTurnoActualPorCaja(Integer cajaId);

    List<TurnoResponseDto> obtenerResumenCierresPorCaja(Integer cajaId, LocalDate fecha);

    TurnoCaja obtenerTurnoAbiertoDelUsuario(Usuario usuario);
}