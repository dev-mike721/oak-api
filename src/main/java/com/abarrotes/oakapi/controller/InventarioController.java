package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.AjusteInventarioRequestDto;
import com.abarrotes.oakapi.dto.response.AlertasCaducidadResponseDto;
import com.abarrotes.oakapi.dto.response.LoteResponseDto;
import com.abarrotes.oakapi.dto.response.MovimientoInventarioResponseDto;
import com.abarrotes.oakapi.dto.response.ValorizacionResponseDto;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import com.abarrotes.oakapi.service.InventarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class InventarioController {

    private final InventarioService inventarioService;

    @GetMapping("/lotes/{productoId}")
    public ResponseEntity<List<LoteResponseDto>> lotesPorProducto(@PathVariable Integer productoId) {
        return ResponseEntity.ok(inventarioService.lotesPorProducto(productoId));
    }

    @PostMapping("/ajustes")
    public ResponseEntity<MovimientoInventarioResponseDto> registrarAjuste(
            @Valid @RequestBody AjusteInventarioRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventarioService.registrarAjuste(request));
    }

    @GetMapping("/alertas-caducidad")
    public ResponseEntity<AlertasCaducidadResponseDto> alertasCaducidad(
            @RequestParam(defaultValue = "30") Integer dias) {
        return ResponseEntity.ok(inventarioService.alertasCaducidad(dias));
    }

    @GetMapping("/valorizacion")
    public ResponseEntity<ValorizacionResponseDto> valorizacion() {
        return ResponseEntity.ok(inventarioService.valorizacion());
    }

    @GetMapping("/movimientos")
    public ResponseEntity<Page<MovimientoInventarioResponseDto>> historial(
            @RequestParam(required = false) Integer loteId,
            @RequestParam(required = false) Integer productoId,
            @RequestParam(required = false) TipoMovimientoInventario tipo,
            @PageableDefault(size = 20) Pageable pageable) {
        Pageable pageableOrdenado = pageable.getSort().isUnsorted()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "fechaHora"))
                : pageable;
        return ResponseEntity.ok(inventarioService.historial(loteId, productoId, tipo, pageableOrdenado));
    }
}
