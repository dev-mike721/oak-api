package com.abarrotes.oakapi;

import com.abarrotes.oakapi.dto.request.CierreTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.SolicitarTurnoRequestDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.model.Caja;
import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.EstadoSolicitudTurno;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import com.abarrotes.oakapi.model.enums.Rol;
import com.abarrotes.oakapi.repository.CajaRepository;
import com.abarrotes.oakapi.repository.TurnoCajaRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.service.TurnoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
class TurnoCierreIntegrationTest {

    private static final String SUFIJO = String.valueOf(System.nanoTime());

    @Autowired
    private TurnoService turnoService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CajaRepository cajaRepository;
    @Autowired
    private TurnoCajaRepository turnoRepository;

    private Integer usuarioId;
    private Integer cajaId;

    @BeforeEach
    void sembrarDatos() {
        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Tester Turno")
                .username("turno_" + SUFIJO)
                .passwordHash("x")
                .rol(Rol.ADMINISTRADOR)
                .activo(true)
                .build());
        usuarioId = usuario.getId();

        Caja caja = new Caja();
        caja.setNombre("Caja " + SUFIJO);
        caja.setUbicacion("Test");
        caja.setActiva(true);
        cajaId = cajaRepository.save(caja).getId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
    }

    private TurnoCaja abrirTurno(BigDecimal montoInicial) {
        return turnoRepository.save(TurnoCaja.builder()
                .usuario(usuarioRepository.findById(usuarioId).orElseThrow())
                .caja(cajaRepository.findById(cajaId).orElseThrow())
                .montoInicial(montoInicial)
                .fechaApertura(LocalDateTime.now())
                .estado(EstadoTurno.ABIERTO)
                .estadoSolicitud(EstadoSolicitudTurno.AUTORIZADO)
                .solicitadoPor(usuarioRepository.findById(usuarioId).orElseThrow())
                .build());
    }

    private CierreTurnoRequestDto cierre(BigDecimal fisico) {
        CierreTurnoRequestDto dto = new CierreTurnoRequestDto();
        dto.setMontoFisicoConteo(fisico);
        return dto;
    }

    /**
     * Regresión B13: calcularVentasEfectivo() usaba "FROM Ventas" con mayúscula.
     * Con lower_case_table_names=0 en MariaDB eso es error 1146 y rompía el cierre.
     */
    @Test
    void cierraTurnoSinVentasYCalculaMontoSistema() {
        TurnoCaja turno = abrirTurno(new BigDecimal("500.00"));

        var resultado = turnoService.cerrar(turno.getId(), cierre(new BigDecimal("500.00")));

        assertEquals(EstadoTurno.CERRADO, resultado.getEstado());
        assertEquals(0, new BigDecimal("500.00").compareTo(resultado.getMontoSistemaCalculado()));
        assertEquals(0, BigDecimal.ZERO.compareTo(resultado.getDiferencia()));
    }

    /**
     * El resumen es plural: debe devolver TODOS los turnos cerrados del día, no el primero.
     * Regresión B15.
     */
    @Test
    void resumenCierresDevuelveTodosLosTurnosDelDia() {
        TurnoCaja t1 = abrirTurno(new BigDecimal("100.00"));
        TurnoCaja t2 = abrirTurno(new BigDecimal("200.00"));
        TurnoCaja t3 = abrirTurno(new BigDecimal("300.00"));

        turnoService.cerrar(t1.getId(), cierre(new BigDecimal("100.00")));
        turnoService.cerrar(t2.getId(), cierre(new BigDecimal("200.00")));
        turnoService.cerrar(t3.getId(), cierre(new BigDecimal("300.00")));

        var resumen = turnoService.obtenerResumenCierresPorCaja(cajaId, LocalDate.now());

        assertEquals(3, resumen.size(), "Debe devolver los 3 turnos cerrados, no uno");
        assertTrue(resumen.stream().allMatch(t -> t.getEstado() == EstadoTurno.CERRADO));
    }

    @Test
    void resumenCierresVacioSinTurnos() {
        assertTrue(turnoService.obtenerResumenCierresPorCaja(cajaId, LocalDate.now()).isEmpty());
    }

    /**
     * Regresión B1: el filtro incluía CERRADO, así que con un solo turno cerrado
     * histórico el usuario nunca podía volver a abrir.
     */
    @Test
    void puedeAbrirSegundoTurnoDespuesDeCerrarElPrimero() {
        TurnoCaja primero = abrirTurno(new BigDecimal("100.00"));
        turnoService.cerrar(primero.getId(), cierre(new BigDecimal("100.00")));

        SolicitarTurnoRequestDto solicitud = new SolicitarTurnoRequestDto();
        solicitud.setCajaId(cajaId);
        solicitud.setMontoInicial(new BigDecimal("50.00"));

        var segundo = turnoService.solicitarApertura(solicitud);

        assertEquals(EstadoTurno.ABIERTO, segundo.getEstado());
    }

    @Test
    void noPermiteDosTurnosAbiertosSimultaneos() {
        abrirTurno(new BigDecimal("100.00"));

        SolicitarTurnoRequestDto solicitud = new SolicitarTurnoRequestDto();
        solicitud.setCajaId(cajaId);
        solicitud.setMontoInicial(new BigDecimal("50.00"));

        assertThrows(BusinessRuleException.class, () -> turnoService.solicitarApertura(solicitud));
    }

    @Test
    void cerrarTurnoYaCerradoFalla() {
        TurnoCaja turno = abrirTurno(new BigDecimal("100.00"));
        turnoService.cerrar(turno.getId(), cierre(new BigDecimal("100.00")));

        assertThrows(BusinessRuleException.class, () -> turnoService.cerrar(turno.getId(), cierre(new BigDecimal("100.00"))));
    }
}