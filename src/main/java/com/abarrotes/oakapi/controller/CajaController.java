package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.CajaRequestDto;
import com.abarrotes.oakapi.dto.response.CajaResponseDto;
import com.abarrotes.oakapi.service.CajaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cajas")
@RequiredArgsConstructor
public class CajaController {

    private final CajaService cajaService;

    @GetMapping
    public ResponseEntity<List<CajaResponseDto>> obtenerTodas() {
        return ResponseEntity.ok(cajaService.obtenerTodas());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CajaResponseDto>> obtenerActivas() {
        return ResponseEntity.ok(cajaService.obtenerActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(cajaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CajaResponseDto> crear(@Valid @RequestBody CajaRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CajaResponseDto> actualizar(@PathVariable Integer id, @Valid @RequestBody CajaRequestDto request) {
        return ResponseEntity.ok(cajaService.actualizar(id, request));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        cajaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable Integer id) {
        cajaService.activar(id);
        return ResponseEntity.noContent().build();
    }
}