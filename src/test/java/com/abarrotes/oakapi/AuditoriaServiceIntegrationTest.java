package com.abarrotes.oakapi;

import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.Rol;
import com.abarrotes.oakapi.repository.LogAuditoriaRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.service.AuditoriaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
class AuditoriaServiceIntegrationTest {

    private static final String SUFIJO = String.valueOf(System.nanoTime());

    @Autowired
    private AuditoriaService auditoriaService;
    @Autowired
    private LogAuditoriaRepository logAuditoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario;

    @BeforeEach
    void sembrar() {
        usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Auditor")
                .username("audit_" + SUFIJO)
                .passwordHash("x")
                .rol(Rol.ADMINISTRADOR)
                .activo(true)
                .build());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        usuario.getUsername(), null,
                        List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))));
    }

    @Test
    void registraLaAccionConUsuarioYDetalles() {
        auditoriaService.registrar("VENTA_CANCELADA", Map.of("uuid", "abc-123", "total", 250.00));

        var logs = logAuditoriaRepository.findAllByOrderByFechaHoraDesc();
        assertFalse(logs.isEmpty());

        var log = logs.get(0);
        assertEquals("VENTA_CANCELADA", log.getAccion());
        assertTrue(log.getDetalles().contains("abc-123"));
        assertEquals(usuario.getId(), log.getUsuario().getId());
    }

    @Test
    void resuelveElUsuarioAutenticadoDeLaSesion() {
        auditoriaService.registrar(usuario, "TURNO_CERRADO", Map.of("turnoId", 7));

        var log = logAuditoriaRepository.findByAccionOrderByFechaHoraDesc("TURNO_CERRADO").get(0);
        assertEquals(usuario.getUsername(), log.getUsuario().getUsername());
    }

    @Test
    void filtraPorAccion() {
        auditoriaService.registrar("TURNO_AUTORIZADO", Map.of("turnoId", 1));
        auditoriaService.registrar("VENTA_CANCELADA", Map.of("uuid", "x"));
        auditoriaService.registrar("TURNO_RECHAZADO", Map.of("turnoId", 2));

        var filtrados = logAuditoriaRepository.findByAccionOrderByFechaHoraDesc("TURNO_AUTORIZADO");
        assertEquals(1, filtrados.size());
        assertEquals("TURNO_AUTORIZADO", filtrados.get(0).getAccion());
    }

    @Test
    void noLanzaSiNoHaySesionIniciada() {
        SecurityContextHolder.clearContext();

        // Debe ser tolerante: la auditoria jamas debe tumbar la operacion de negocio
        assertDoesNotThrow(() -> auditoriaService.registrar("ALGO", Map.of("k", "v")));
        assertTrue(logAuditoriaRepository.findAllByOrderByFechaHoraDesc().isEmpty());
    }

    @Test
    void serializaLosDetallesComoTexto() {
        auditoriaService.registrar(usuario, "PAGO_CREDITO_ANULADO",
                Map.of("pagoId", 12, "monto", 100.00));

        var log = logAuditoriaRepository.findByAccionOrderByFechaHoraDesc("PAGO_CREDITO_ANULADO").get(0);
        assertTrue(log.getDetalles().contains("pagoId"));
        assertTrue(log.getDetalles().contains("12"));
    }
}