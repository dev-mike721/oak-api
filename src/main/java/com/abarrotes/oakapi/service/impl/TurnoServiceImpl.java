package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.AperturaTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.CierreTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.SolicitarTurnoRequestDto;
import com.abarrotes.oakapi.dto.response.TurnoResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Caja;
import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.EstadoSolicitudTurno;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import com.abarrotes.oakapi.repository.CajaRepository;
import com.abarrotes.oakapi.repository.TurnoCajaRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.repository.VentaRepository;
import com.abarrotes.oakapi.service.AuditoriaService;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TurnoServiceImpl implements TurnoService {

    private final TurnoCajaRepository turnoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final CajaRepository cajaRepository;
    private final VentaRepository ventaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<TurnoResponseDto> obtenerTodas(EstadoTurno estado, EstadoSolicitudTurno estadoSolicitud, Integer cajaId, Integer usuarioId,
                                               LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable) {
        LocalDateTime desde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
        LocalDateTime hasta = fechaHasta != null ? fechaHasta.plusDays(1).atStartOfDay() : null;
        return turnoRepository.buscarConFiltros(estado, estadoSolicitud, cajaId, usuarioId, desde, hasta, pageable)
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
    public TurnoResponseDto solicitarApertura(SolicitarTurnoRequestDto request) {
        Usuario usuario = obtenerUsuarioActual();
        
        if (turnoRepository.existsByUsuarioIdAndEstado(usuario.getId(), EstadoTurno.ABIERTO)) {
            throw new BusinessRuleException("El usuario ya tiene un turno abierto o pendiente de autorización. Ciérrelo antes de solicitar otro");
        }

        Caja caja = cajaRepository.findById(request.getCajaId())
                .orElseThrow(() -> new ResourceNotFoundException("Caja no encontrada con ID: " + request.getCajaId()));

        if (!caja.getActiva()) {
            throw new BusinessRuleException("La caja seleccionada no está activa");
        }

        TurnoCaja turno = turnoRepository.save(TurnoCaja.builder()
                .usuario(usuario)
                .caja(caja)
                .fechaApertura(LocalDateTime.now())
                .montoInicial(request.getMontoInicial())
                .estado(EstadoTurno.ABIERTO)
                .estadoSolicitud(EstadoSolicitudTurno.SOLICITADO)
                .solicitadoPor(usuario)
                .build());

        auditoriaService.registrar(usuario, "TURNO_SOLICITADO", Map.of(
                "turnoId", turno.getId(),
                "cajaId", caja.getId(),
                "montoInicial", request.getMontoInicial()));
        return mapToResponse(turno);
    }

    @Override
    @Transactional
    public TurnoResponseDto autorizar(Integer turnoId) {
        Usuario admin = obtenerUsuarioActual();
        if (!admin.getRol().name().equals("ADMINISTRADOR") && !admin.getRol().name().equals("GERENTE")) {
            throw new BusinessRuleException("Solo administradores y gerentes pueden autorizar turnos");
        }

        TurnoCaja turno = obtenerTurno(turnoId);
        if (turno.getEstadoSolicitud() != EstadoSolicitudTurno.SOLICITADO) {
            throw new BusinessRuleException("Solo se pueden autorizar turnos en estado SOLICITADO");
        }

        turno.setEstado(EstadoTurno.ABIERTO);
        turno.setEstadoSolicitud(EstadoSolicitudTurno.AUTORIZADO);
        turno.setAutorizadoPor(admin);
        turno.setAutorizadoEn(LocalDateTime.now());
        auditoriaService.registrar(admin, "TURNO_AUTORIZADO", Map.of(
                "turnoId", turnoId,
                "cajaId", turno.getCaja().getId(),
                "montoInicial", turno.getMontoInicial()));
        return mapToResponse(turno);
    }

    @Override
    @Transactional
    public TurnoResponseDto rechazar(Integer turnoId, String motivo) {
        Usuario admin = obtenerUsuarioActual();
        if (!admin.getRol().name().equals("ADMINISTRADOR") && !admin.getRol().name().equals("GERENTE")) {
            throw new BusinessRuleException("Solo administradores y gerentes pueden rechazar turnos");
        }

        TurnoCaja turno = obtenerTurno(turnoId);
        if (turno.getEstadoSolicitud() != EstadoSolicitudTurno.SOLICITADO) {
            throw new BusinessRuleException("Solo se pueden rechazar turnos en estado SOLICITADO");
        }

        turno.setEstadoSolicitud(EstadoSolicitudTurno.RECHAZADO);
        turno.setAutorizadoPor(admin);
        turno.setAutorizadoEn(LocalDateTime.now());
        turno.setMotivoRechazo(motivo);
        auditoriaService.registrar(admin, "TURNO_RECHAZADO", Map.of(
                "turnoId", turnoId,
                "cajaId", turno.getCaja().getId(),
                "motivo", Objects.requireNonNullElse(motivo, "")));
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
        auditoriaService.registrar(turno.getUsuario(), "TURNO_CERRADO", Map.of(
                "turnoId", id,
                "cajaId", turno.getCaja().getId(),
                "montoInicial", turno.getMontoInicial(),
                "montoSistema", montoSistema,
                "montoFisico", request.getMontoFisicoConteo(),
                "diferencia", diferencia));
        return mapToResponse(turno);
    }

    @Override
    @Transactional(readOnly = true)
    public TurnoResponseDto obtenerTurnoActualPorCaja(Integer cajaId) {
        return turnoRepository.findFirstByCajaIdAndEstadoOrderByIdDesc(cajaId, EstadoTurno.ABIERTO)
                .or(() -> turnoRepository.findFirstByCajaIdAndEstadoSolicitudInOrderByIdDesc(cajaId, 
                        List.of(EstadoSolicitudTurno.SOLICITADO, EstadoSolicitudTurno.AUTORIZADO)))
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No hay turno activo o pendiente en esta caja"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TurnoResponseDto> obtenerResumenCierresPorCaja(Integer cajaId, LocalDate fecha) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
        return turnoRepository.findByCajaIdAndRangoFechaCierre(cajaId, inicio, fin).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BigDecimal calcularVentasEfectivo(Integer turnoId) {
        try {
            BigDecimal ventas = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(v.total), 0) FROM ventas v " +
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

    @Override
    @Transactional(readOnly = true)
    public TurnoCaja obtenerTurnoAbiertoDelUsuario(Usuario usuario) {
        return turnoRepository.findFirstByUsuarioIdAndEstadoOrderByIdDesc(usuario.getId(), EstadoTurno.ABIERTO)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario no tiene un turno de caja abierto"));
    }

    private TurnoResponseDto mapToResponse(TurnoCaja t) {
        return TurnoResponseDto.builder()
                .id(t.getId())
                .usuarioId(t.getUsuario().getId())
                .usuarioUsername(t.getUsuario().getUsername())
                .cajaId(t.getCaja() != null ? t.getCaja().getId() : null)
                .cajaNombre(t.getCaja() != null ? t.getCaja().getNombre() : null)
                .fechaApertura(t.getFechaApertura())
                .montoInicial(t.getMontoInicial())
                .fechaCierre(t.getFechaCierre())
                .montoFisicoConteo(t.getMontoFisicoConteo())
                .montoSistemaCalculado(t.getMontoSistemaCalculado())
                .diferencia(t.getDiferencia())
                .estado(t.getEstado())
                .estadoSolicitud(t.getEstadoSolicitud())
                .solicitadoPorId(t.getSolicitadoPor() != null ? t.getSolicitadoPor().getId() : null)
                .solicitadoPorUsername(t.getSolicitadoPor() != null ? t.getSolicitadoPor().getUsername() : null)
                .autorizadoPorId(t.getAutorizadoPor() != null ? t.getAutorizadoPor().getId() : null)
                .autorizadoPorUsername(t.getAutorizadoPor() != null ? t.getAutorizadoPor().getUsername() : null)
                .autorizadoEn(t.getAutorizadoEn())
                .motivoRechazo(t.getMotivoRechazo())
                .build();
    }
}