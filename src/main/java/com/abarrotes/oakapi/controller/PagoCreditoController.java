package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto;
import com.abarrotes.oakapi.dto.response.CuentaCorrienteResponseDto;
import com.abarrotes.oakapi.dto.response.PagoCreditoAplicacionResponseDto;
import com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto;
import com.abarrotes.oakapi.service.PagoCreditoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pagos-credito")
@RequiredArgsConstructor
public class PagoCreditoController {

    private final PagoCreditoService pagoCreditoService;

    @PostMapping
    public ResponseEntity<PagoCreditoResponseDto> registrarPago(@Valid @RequestBody PagoCreditoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoCreditoService.registrarPago(request));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PagoCreditoResponseDto>> obtenerPorCliente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(pagoCreditoService.obtenerPorCliente(clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PagoCreditoResponseDto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoCreditoService.obtenerPorId(id));
    }

    @GetMapping("/{id}/aplicaciones")
    public ResponseEntity<List<PagoCreditoAplicacionResponseDto>> obtenerAplicaciones(@PathVariable Integer id) {
        return ResponseEntity.ok(pagoCreditoService.obtenerAplicaciones(id));
    }

    @PostMapping("/{id}/aplicar-fifo")
    public ResponseEntity<Void> aplicarFIFO(@PathVariable Integer id) {
        pagoCreditoService.aplicarFIFO(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/cliente/{clienteId}/cuenta-corriente")
    public ResponseEntity<CuentaCorrienteResponseDto> obtenerCuentaCorriente(@PathVariable Integer clienteId) {
        return ResponseEntity.ok(pagoCreditoService.obtenerCuentaCorriente(clienteId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anularPago(@PathVariable Integer id) {
        pagoCreditoService.anularPago(id);
        return ResponseEntity.noContent().build();
    }
}