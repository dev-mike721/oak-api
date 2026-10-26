package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.model.LogAuditoria;
import com.abarrotes.oakapi.repository.LogAuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auditoria")
@RequiredArgsConstructor
public class AuditoriaController {

    private final LogAuditoriaRepository logAuditoriaRepository;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> listar(
            @RequestParam(required = false) String accion) {
        List<LogAuditoria> logs = (accion == null || accion.isBlank())
                ? logAuditoriaRepository.findAllByOrderByFechaHoraDesc()
                : logAuditoriaRepository.findByAccionOrderByFechaHoraDesc(accion);

        List<Map<String, Object>> salida = logs.stream()
                .map(l -> {
                    Map<String, Object> fila = new HashMap<>();
                    fila.put("id", l.getId());
                    fila.put("accion", l.getAccion());
                    fila.put("detalles", l.getDetalles());
                    fila.put("usuario", l.getUsuario().getUsername());
                    fila.put("autorizadoPor",
                            l.getAutorizadoPor() != null ? l.getAutorizadoPor().getUsername() : null);
                    fila.put("fechaHora", l.getFechaHora().toString());
                    return fila;
                })
                .toList();

        return ResponseEntity.ok(salida);
    }
}
