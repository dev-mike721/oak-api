package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.VentaRequestDto;
import com.abarrotes.oakapi.dto.response.VentaResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.service.VentaService;
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
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<Page<VentaResponseDto>> obtenerTodas(
            @RequestParam(required = false) MetodoPago metodoPago,
            @RequestParam(required = false) EstadoVenta estado,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer turnoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Pageable pageable) {
        return ResponseEntity.ok(ventaService.obtenerTodas(
                metodoPago, estado, usuarioId, turnoId, fechaDesde, fechaHasta, pageable));
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<VentaResponseDto> obtenerPorId(@PathVariable String uuid) {
        return ResponseEntity.ok(ventaService.obtenerPorId(uuid));
    }

    @PostMapping
    public ResponseEntity<VentaResponseDto> registrar(@Valid @RequestBody VentaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.registrar(request));
    }

    @PutMapping("/{uuid}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable String uuid) {
        ventaService.cancelar(uuid);
        return ResponseEntity.noContent().build();
    }
}