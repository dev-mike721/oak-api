package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.ListaPrecioDetalleRequestDto;
import com.abarrotes.oakapi.dto.request.ListaPrecioRequestDto;
import com.abarrotes.oakapi.dto.response.ListaPrecioDetalleResponseDto;
import com.abarrotes.oakapi.dto.response.ListaPrecioResponseDto;
import com.abarrotes.oakapi.dto.response.PrecioResueltoResponseDto;
import com.abarrotes.oakapi.service.ListaPrecioService;
import com.abarrotes.oakapi.service.PrecioResolverService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/listas-precios")
@RequiredArgsConstructor
public class ListaPrecioController {

    private final ListaPrecioService listaPrecioService;
    private final PrecioResolverService precioResolverService;

    @GetMapping
    public ResponseEntity<Page<ListaPrecioResponseDto>> obtenerTodas(Pageable pageable) {
        return ResponseEntity.ok(listaPrecioService.obtenerTodas(pageable));
    }

    @GetMapping("/vigentes")
    public ResponseEntity<Page<ListaPrecioResponseDto>> obtenerVigentes(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            Pageable pageable) {
        return ResponseEntity.ok(listaPrecioService.obtenerVigentes(fecha != null ? fecha : java.time.LocalDate.now(), pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ListaPrecioResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(listaPrecioService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<ListaPrecioResponseDto> crear(@Valid @RequestBody ListaPrecioRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listaPrecioService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ListaPrecioResponseDto> actualizar(@PathVariable Integer id, @Valid @RequestBody ListaPrecioRequestDto request) {
        return ResponseEntity.ok(listaPrecioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        listaPrecioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<java.util.List<ListaPrecioDetalleResponseDto>> obtenerDetalles(@PathVariable Integer id) {
        return ResponseEntity.ok(listaPrecioService.obtenerDetalles(id));
    }

    @PostMapping("/{id}/detalles")
    public ResponseEntity<ListaPrecioDetalleResponseDto> agregarDetalle(
            @PathVariable Integer id,
            @Valid @RequestBody ListaPrecioDetalleRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(listaPrecioService.agregarDetalle(id, request));
    }

    @DeleteMapping("/{id}/detalles/{productoId}")
    public ResponseEntity<Void> eliminarDetalle(@PathVariable Integer id, @PathVariable Integer productoId) {
        listaPrecioService.eliminarDetalle(id, productoId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/precios/resolver")
    public ResponseEntity<PrecioResueltoResponseDto> resolverPrecio(
            @RequestParam Integer clienteId,
            @RequestParam Integer productoId,
            @RequestParam BigDecimal cantidad,
            @RequestParam(required = false) LocalDate fecha) {
        return ResponseEntity.ok(precioResolverService.resolvePrecio(clienteId, productoId, cantidad, fecha != null ? fecha : LocalDate.now()));
    }
}