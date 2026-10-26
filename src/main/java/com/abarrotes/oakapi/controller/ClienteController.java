package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.ClientePrecioEspecialRequestDto;
import com.abarrotes.oakapi.dto.request.ClienteRequestDto;
import com.abarrotes.oakapi.dto.response.AgingResponseDto;
import com.abarrotes.oakapi.dto.response.ClientePrecioEspecialResponseDto;
import com.abarrotes.oakapi.dto.response.ClienteResponseDto;
import com.abarrotes.oakapi.dto.response.CuentaCorrienteResponseDto;
import com.abarrotes.oakapi.service.ClienteService;
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
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<Page<ClienteResponseDto>> obtenerTodos(Pageable pageable) {
        return ResponseEntity.ok(clienteService.obtenerTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @GetMapping("/rfc/{rfc}")
    public ResponseEntity<ClienteResponseDto> obtenerPorRfc(@PathVariable String rfc) {
        return ResponseEntity.ok(clienteService.obtenerPorRfc(rfc));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDto> crear(@Valid @RequestBody ClienteRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDto> actualizar(@PathVariable Integer id, @Valid @RequestBody ClienteRequestDto request) {
        return ResponseEntity.ok(clienteService.actualizar(id, request));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id) {
        clienteService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable Integer id) {
        clienteService.reactivar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/cuenta-corriente")
    public ResponseEntity<CuentaCorrienteResponseDto> obtenerCuentaCorriente(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.obtenerCuentaCorriente(id));
    }

    @GetMapping("/aging")
    public ResponseEntity<AgingResponseDto> obtenerAging(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte) {
        return ResponseEntity.ok(clienteService.obtenerAging(fechaCorte != null ? fechaCorte : LocalDate.now()));
    }

    @PostMapping("/{clienteId}/precios-especiales")
    public ResponseEntity<ClientePrecioEspecialResponseDto> crearPrecioEspecial(
            @PathVariable Integer clienteId,
            @Valid @RequestBody ClientePrecioEspecialRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crearPrecioEspecial(clienteId, request));
    }

    @GetMapping("/{clienteId}/precios-especiales")
    public ResponseEntity<java.util.List<ClientePrecioEspecialResponseDto>> obtenerPreciosEspeciales(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(clienteService.obtenerPreciosEspeciales(clienteId));
    }

    @DeleteMapping("/{clienteId}/precios-especiales/{productoId}")
    public ResponseEntity<Void> eliminarPrecioEspecial(@PathVariable Integer clienteId, @PathVariable Integer productoId) {
        clienteService.eliminarPrecioEspecial(clienteId, productoId);
        return ResponseEntity.noContent().build();
    }
}