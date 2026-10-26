package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.model.LogAuditoria;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.repository.LogAuditoriaRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final LogAuditoriaRepository logAuditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Comparte la transaccion de la operacion de negocio (REQUIRED): si el negocio
     * hace rollback, no quedo registrada una accion que en realidad no ocurrio.
     */
    @Override
    @Transactional
    public void registrar(Usuario usuario, String accion, Map<String, Object> detalles) {
        if (usuario == null) {
            usuario = usuarioAutenticado();
        }
        if (usuario == null) {
            log.warn("Auditoria sin usuario resoluble para la accion {}: {}", accion, detalles);
            return;
        }

        try {
            logAuditoriaRepository.save(LogAuditoria.builder()
                    .usuario(usuario)
                    .accion(accion)
                    .detalles(serialize(detalles))
                    .build());
        } catch (RuntimeException e) {
            // La auditoria nunca debe tumbar la operacion de negocio
            log.error("No se pudo registrar la auditoria de {}: {}", accion, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void registrar(String accion, Map<String, Object> detalles) {
        registrar(usuarioAutenticado(), accion, detalles);
    }

    @Override
    @Transactional
    public void registrar(String accion, String detalles) {
        registrar(usuarioAutenticado(), accion, Map.of("detalle", String.valueOf(detalles)));
    }

    private Usuario usuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return usuarioRepository.findByUsernameAndActivoTrue(auth.getName()).orElse(null);
    }

    private String serialize(Map<String, Object> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder("{");
        detalles.forEach((k, v) -> {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append('"').append(k).append("\": \"").append(v).append('"');
        });
        return sb.append('}').toString();
    }
}