package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.AperturaTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.CierreTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.SolicitarTurnoRequestDto;
import com.abarrotes.oakapi.dto.response.TurnoResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoSolicitudTurno;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import com.abarrotes.oakapi.service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<Page<TurnoResponseDto>> obtenerTodas(
            @RequestParam(required = false) EstadoTurno estado,
            @RequestParam(required = false) EstadoSolicitudTurno estadoSolicitud,
            @RequestParam(required = false) Integer cajaId,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Pageable pageable) {
        return ResponseEntity.ok(turnoService.obtenerTodas(estado, estadoSolicitud, cajaId, usuarioId, fechaDesde, fechaHasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(turnoService.obtenerPorId(id));
    }

    @GetMapping("/actual")
    public ResponseEntity<TurnoResponseDto> obtenerActual() {
        return ResponseEntity.ok(turnoService.obtenerActual());
    }

    @PostMapping("/solicitar")
    public ResponseEntity<TurnoResponseDto> solicitarApertura(@Valid @RequestBody SolicitarTurnoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.solicitarApertura(request));
    }

    @PostMapping("/{id}/autorizar")
    public ResponseEntity<TurnoResponseDto> autorizar(@PathVariable Integer id) {
        return ResponseEntity.ok(turnoService.autorizar(id));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<TurnoResponseDto> rechazar(@PathVariable Integer id, @RequestParam String motivo) {
        return ResponseEntity.ok(turnoService.rechazar(id, motivo));
    }

    @GetMapping("/caja/{cajaId}/actual")
    public ResponseEntity<TurnoResponseDto> obtenerTurnoActualPorCaja(@PathVariable Integer cajaId) {
        return ResponseEntity.ok(turnoService.obtenerTurnoActualPorCaja(cajaId));
    }

    @GetMapping("/caja/{cajaId}/resumen-cierres")
    public ResponseEntity<List<TurnoResponseDto>> obtenerResumenCierresPorCaja(
            @PathVariable Integer cajaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.obtenerResumenCierresPorCaja(cajaId, fecha));
    }

    @PutMapping("/{id}/cierre")
    public ResponseEntity<TurnoResponseDto> cerrar(@PathVariable Integer id,
                                                   @Valid @RequestBody CierreTurnoRequestDto request) {
        return ResponseEntity.ok(turnoService.cerrar(id, request));
    }
}