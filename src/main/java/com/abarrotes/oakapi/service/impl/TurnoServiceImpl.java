package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.AperturaTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.CierreTurnoRequestDto;
import com.abarrotes.oakapi.dto.response.TurnoResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import com.abarrotes.oakapi.repository.TurnoCajaRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {

    private final TurnoCajaRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<TurnoResponseDto> obtenerTodas(EstadoTurno estado, Integer usuarioId,
                                               LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        LocalDateTime desde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = fechaHasta != null ? fechaHasta.plusDays(1).atStartOfDay() : null;
        return turnoRepository.buscarConFiltros(estado, usuarioId, desde, hasta, pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResponseDto obtenerPorId(Integer id) {
        return mapToResponse(obtenerTurno(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResponseDto obtenerActual() {
        Integer usuarioId = obtenerUsuarioActual().getId();
        return turnoRepository.findFirstByUsuarioIdAndEstadoOrderByIdDesc(usuarioId, EstadoTurno.ABIERTO)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene un turno de caja abierto"));
    }

    @Override
    @Transactional
    public TurnoResponseDto abrir(AperturaTurnoRequestDto request) {
        Usuario usuario = obtenerUsuarioActual();
        if (turnoRepository.existsByUsuarioIdAndEstado(usuario.getId(), EstadoTurno.ABIERTO)) {
            throw new BusinessRuleException("El usuario ya tiene un turno de caja abierto, ciérralo antes de abrir otro");
        }

        TurnoCaja turno = turnoRepository.save(TurnoCaja.builder()
                .usuario(usuario)
                .fechaApertura(LocalDateTime.now())
                .montoInicial(request.getMontoInicial())
                .estado(EstadoTurno.ABIERTO)
                .build());
        return mapToResponse(turno);
    }

    @Override
    @Transactional
    public TurnoResponseDto cerrar(Integer id, CierreTurnoRequestDto request) {
        TurnoCaja turno = obtenerTurno(id);
        if (turno.getEstado() == EstadoTurno.CERRADO) {
            throw new BusinessRuleException("El turno ya está cerrado");
        }

        BigDecimal montoSistema = turno.getMontoInicial().add(calcularVentasEfectivo(id));
        BigDecimal diferencia = request.getMontoFisicoConteo().subtract(montoSistema)
                .setScale(2, RoundingMode.HALF_UP);

        turno.setFechaCierre(LocalDateTime.now());
        turno.setMontoFisicoConteo(request.getMontoFisicoConteo());
        turno.setMontoSistemaCalculado(montoSistema);
        turno.setDiferencia(diferencia);
        turno.setEstado(EstadoTurno.CERRADO);
        return mapToResponse(turno);
    }

    private BigDecimal calcularVentasEfectivo(Integer turnoId) {
        try {
            BigDecimal ventas = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(v.total), 0) FROM Ventas v " +
                    "WHERE v.turno_id = ? AND v.estado = 'COMPLETADA' AND v.metodo_pago = 'EFECTIVO'",
                    BigDecimal.class, turnoId);
            return ventas != null ? ventas : BigDecimal.ZERO;
        } catch (EmptyResultDataAccessException e) {
            return BigDecimal.ZERO;
        }
    }

    private TurnoCaja obtenerTurno(Integer id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno no encontrado con ID: " + id));
    }

    private Usuario obtenerUsuarioActual() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario autenticado no encontrado: " + username));
    }

    private TurnoResponseDto mapToResponse(TurnoCaja t) {
        return TurnoResponseDto.builder()
                .id(t.getId())
                .usuarioId(t.getUsuario().getId())
                .usuarioUsername(t.getUsuario().getUsername())
                .fechaApertura(t.getFechaApertura())
                .montoInicial(t.getMontoInicial())
                .fechaCierre(t.getFechaCierre())
                .montoFisicoConteo(t.getMontoFisicoConteo())
                .montoSistemaCalculado(t.getMontoSistemaCalculado())
                .diferencia(t.getDiferencia())
                .estado(t.getEstado())
                .build();
    }
}