package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.CompraRequestDto;
import com.abarrotes.oakapi.dto.response.CompraResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoCompra;
import com.abarrotes.oakapi.service.CompraService;
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
@RequestMapping("/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @GetMapping
    public ResponseEntity<Page<CompraResponseDto>> obtenerTodas(
            @RequestParam(required = false) EstadoCompra estado,
            @RequestParam(required = false) Integer proveedorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
            Pageable pageable) {
        return ResponseEntity.ok(compraService.obtenerTodas(estado, proveedorId, fechaDesde, fechaHasta, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(compraService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<CompraResponseDto> crear(@Valid @RequestBody CompraRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compraService.crear(request));
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<Void> pagar(@PathVariable Integer id) {
        compraService.pagar(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        compraService.cancelar(id);
        return ResponseEntity.ok().build();
    }
}