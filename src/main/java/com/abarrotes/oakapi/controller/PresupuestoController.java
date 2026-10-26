package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.ConvertirPresupuestoRequestDto;
import com.abarrotes.oakapi.dto.request.PresupuestoRequestDto;
import com.abarrotes.oakapi.dto.response.PresupuestoResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import com.abarrotes.oakapi.service.PresupuestoService;
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
@RequestMapping("/presupuestos")
@RequiredArgsConstructor
public class PresupuestoController {

    private final PresupuestoService presupuestoService;

    @GetMapping
    public ResponseEntity<Page<PresupuestoResponseDto>> obtenerTodos(
            @RequestParam(required = false) EstadoPresupuesto estado,
            @RequestParam(required = false) Integer clienteId,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Pageable pageable) {
        return ResponseEntity.ok(presupuestoService.obtenerTodos(estado, clienteId, usuarioId, fechaDesde, fechaHasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PresupuestoResponseDto> obtenerPorId(@PathVariable String id) {
        return ResponseEntity.ok(presupuestoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<PresupuestoResponseDto> crear(@Valid @RequestBody PresupuestoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(presupuestoService.crear(request));
    }

    @PostMapping("/{id}/enviar")
    public ResponseEntity<PresupuestoResponseDto> enviar(@PathVariable String id) {
        return ResponseEntity.ok(presupuestoService.enviar(id));
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<PresupuestoResponseDto> aprobar(@PathVariable String id) {
        return ResponseEntity.ok(presupuestoService.aprobar(id));
    }

    @PostMapping("/{id}/convertir-venta")
    public ResponseEntity<PresupuestoResponseDto> convertirAVenta(
            @PathVariable String id,
            @Valid @RequestBody ConvertirPresupuestoRequestDto request) {
        return ResponseEntity.ok(presupuestoService.convertirAVenta(id, request));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PresupuestoResponseDto> cancelar(@PathVariable String id) {
        return ResponseEntity.ok(presupuestoService.cancelar(id));
    }

    @PostMapping("/vencer")
    public ResponseEntity<Void> vencerPresupuestos() {
        presupuestoService.vencerPresupuestos();
        return ResponseEntity.noContent().build();
    }
}