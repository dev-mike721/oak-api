package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.ImpuestoRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoImpuestoRequestDto;
import com.abarrotes.oakapi.dto.response.ImpuestoResponseDto;
import com.abarrotes.oakapi.dto.response.ProductoImpuestoResponseDto;
import com.abarrotes.oakapi.service.ImpuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/impuestos")
@RequiredArgsConstructor
public class ImpuestoController {

    private final ImpuestoService impuestoService;

    @GetMapping
    public ResponseEntity<List<ImpuestoResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(impuestoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImpuestoResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(impuestoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ImpuestoResponseDto> crear(@Valid @RequestBody ImpuestoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impuestoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImpuestoResponseDto> actualizar(@PathVariable Integer id, @Valid @RequestBody ImpuestoRequestDto request) {
        return ResponseEntity.ok(impuestoService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        impuestoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/productos/{productoId}")
    public ResponseEntity<ProductoImpuestoResponseDto> asignarImpuestoAProducto(
            @PathVariable Integer productoId,
            @Valid @RequestBody ProductoImpuestoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(impuestoService.asignarImpuestoAProducto(productoId, request));
    }

    @GetMapping("/productos/{productoId}")
    public ResponseEntity<List<ProductoImpuestoResponseDto>> obtenerImpuestosDeProducto(@PathVariable Integer productoId) {
        return ResponseEntity.ok(impuestoService.obtenerImpuestosDeProducto(productoId));
    }

    @DeleteMapping("/productos/{productoId}/{impuestoId}")
    public ResponseEntity<Void> quitarImpuestoDeProducto(@PathVariable Integer productoId, @PathVariable Integer impuestoId) {
        impuestoService.quitarImpuestoDeProducto(productoId, impuestoId);
        return ResponseEntity.noContent().build();
    }
}