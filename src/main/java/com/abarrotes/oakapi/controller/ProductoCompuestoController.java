package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.ComponenteRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoCompuestoRequestDto;
import com.abarrotes.oakapi.dto.response.ProductoCompuestoResponseDto;
import com.abarrotes.oakapi.service.ProductoCompuestoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoCompuestoController {

    private final ProductoCompuestoService productoCompuestoService;

    @GetMapping("/{padreId}/componentes")
    public ResponseEntity<ProductoCompuestoResponseDto> obtenerReceta(@PathVariable Integer padreId) {
        return ResponseEntity.ok(productoCompuestoService.obtenerReceta(padreId));
    }

    @PostMapping("/{padreId}/componentes")
    public ResponseEntity<ProductoCompuestoResponseDto> agregarComponente(@PathVariable Integer padreId,
                                                                          @Valid @RequestBody ComponenteRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productoCompuestoService.agregarComponente(padreId, request));
    }

    @PutMapping("/{padreId}/componentes")
    public ResponseEntity<ProductoCompuestoResponseDto> reemplazarReceta(@PathVariable Integer padreId,
                                                                         @Valid @RequestBody ProductoCompuestoRequestDto request) {
        return ResponseEntity.ok(productoCompuestoService.reemplazarReceta(padreId, request));
    }

    @DeleteMapping("/{padreId}/componentes/{hijoId}")
    public ResponseEntity<Void> eliminarComponente(@PathVariable Integer padreId, @PathVariable Integer hijoId) {
        productoCompuestoService.eliminarComponente(padreId, hijoId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{padreId}/componentes")
    public ResponseEntity<Void> vaciarReceta(@PathVariable Integer padreId) {
        productoCompuestoService.vaciarReceta(padreId);
        return ResponseEntity.noContent().build();
    }
}
