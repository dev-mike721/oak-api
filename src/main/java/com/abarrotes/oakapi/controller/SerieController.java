package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.SerieEstadoRequestDto;
import com.abarrotes.oakapi.dto.request.SerieRequestDto;
import com.abarrotes.oakapi.dto.response.SerieResponseDto;
import com.abarrotes.oakapi.dto.response.SeriesDisponiblesResponseDto;
import com.abarrotes.oakapi.service.SerieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/series")
@RequiredArgsConstructor
public class SerieController {

    private final SerieService serieService;

    @PostMapping("/compra")
    public ResponseEntity<List<SerieResponseDto>> registrarSeriesEnCompra(
            @Valid @RequestBody SerieRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                serieService.registrarSeriesEnCompra(request.getProductoId(), request.getLoteId(), request.getSeries())
        );
    }

    @GetMapping("/disponibles-venta")
    public ResponseEntity<List<SerieResponseDto>> obtenerDisponiblesParaVenta(
            @RequestParam Integer productoId,
            @RequestParam(required = false) Integer cantidad) {
        return ResponseEntity.ok(serieService.obtenerDisponiblesParaVenta(productoId, cantidad));
    }

    @PostMapping("/venta")
    public ResponseEntity<List<SerieResponseDto>> venderSeries(
            @RequestParam Integer productoId,
            @RequestParam(required = false) List<String> codigos,
            @RequestParam(required = false) String ventaId) {
        return ResponseEntity.ok(serieService.venderSeries(productoId, codigos, ventaId));
    }

    @PostMapping("/cancelar-venta/{ventaId}")
    public ResponseEntity<List<SerieResponseDto>> liberarSeriesPorCancelacion(@PathVariable String ventaId) {
        return ResponseEntity.ok(serieService.liberarSeriesPorCancelacion(ventaId));
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<SerieResponseDto> cambiarEstado(
            @PathVariable Integer id,
            @Valid @RequestBody SerieEstadoRequestDto request) {
        return ResponseEntity.ok(serieService.cambiarEstado(id, request));
    }

    @GetMapping("/disponibles-generales")
    public ResponseEntity<List<SeriesDisponiblesResponseDto>> obtenerDisponibles(
            @RequestParam Integer productoId,
            @RequestParam(required = false) Integer limite) {
        return ResponseEntity.ok(serieService.obtenerDisponibles(productoId, limite));
    }
}