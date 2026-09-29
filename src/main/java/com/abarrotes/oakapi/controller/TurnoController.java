package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.AperturaTurnoRequestDto;
import com.abarrotes.oakapi.dto.request.CierreTurnoRequestDto;
import com.abarrotes.oakapi.dto.response.TurnoResponseDto;
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

@RestController
@RequestMapping("/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<Page<TurnoResponseDto>> obtenerTodas(
            @RequestParam(required = false) EstadoTurno estado,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Pageable pageable) {
        return ResponseEntity.ok(turnoService.obtenerTodas(estado, usuarioId, fechaDesde, fechaHasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(turnoService.obtenerPorId(id));
    }

    @GetMapping("/actual")
    public ResponseEntity<TurnoResponseDto> obtenerActual() {
        return ResponseEntity.ok(turnoService.obtenerActual());
    }

    @PostMapping
    public ResponseEntity<TurnoResponseDto> abrir(@Valid @RequestBody AperturaTurnoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.abrir(request));
    }

    @PutMapping("/{id}/cierre")
    public ResponseEntity<TurnoResponseDto> cerrar(@PathVariable Integer id,
                                                   @Valid @RequestBody CierreTurnoRequestDto request) {
        return ResponseEntity.ok(turnoService.cerrar(id, request));
    }
}