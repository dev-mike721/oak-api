package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.ProveedorRequestDto;
import com.abarrotes.oakapi.dto.response.ProveedorResponseDto;
import com.abarrotes.oakapi.service.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<Page<ProveedorResponseDto>> obtenerTodos(
            @RequestParam(required = false) String termino,
            Pageable pageable) {
        return ResponseEntity.ok(proveedorService.obtenerTodos(termino, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponseDto> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(proveedorService.buscarPorId(id));
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<ProveedorResponseDto>> obtenerInactivos() {
        return ResponseEntity.ok(proveedorService.obtenerInactivos());
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<ProveedorResponseDto> reactivar(@PathVariable Integer id) {
        return ResponseEntity.ok(proveedorService.reactivar(id));
    }

    @PostMapping
    public ResponseEntity<ProveedorResponseDto> crear(@Valid @RequestBody ProveedorRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponseDto> actualizar(@PathVariable Integer id,
                                                           @Valid @RequestBody ProveedorRequestDto request) {
        return ResponseEntity.ok(proveedorService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        proveedorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}