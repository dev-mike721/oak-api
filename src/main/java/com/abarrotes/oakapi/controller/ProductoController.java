package com.abarrotes.oakapi.controller;
import com.abarrotes.oakapi.dto.request.ProductoRequestDto;
import com.abarrotes.oakapi.dto.response.ProductoResponseDto;
import com.abarrotes.oakapi.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    @GetMapping
    public ResponseEntity<List<ProductoResponseDto>> obtenerTodos() {
        return ResponseEntity.ok(productoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDto> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.buscarPorId(id));
    }

    @GetMapping("/plu/{plu}")
    public ResponseEntity<ProductoResponseDto> buscarPorPlu(@PathVariable String plu) {
        return ResponseEntity.ok(productoService.buscarPorPlu(plu));
    }

    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDto>> buscarPorCategoria(@PathVariable Integer categoriaId) {
        return ResponseEntity.ok(productoService.buscarPorCategoria(categoriaId));
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductoResponseDto>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombre(nombre));
    }

    @GetMapping("/inactivos")
    public ResponseEntity<List<ProductoResponseDto>> obtenerInactivos() {
        return ResponseEntity.ok(productoService.obtenerInactivos());
    }

    @PutMapping("/{id}/reactivar")
    public ResponseEntity<ProductoResponseDto> reactivar(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.reactivarProducto(id));
    }

    @PostMapping
    public ResponseEntity<ProductoResponseDto> crear(@Valid @RequestBody ProductoRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDto> actualizar(@PathVariable Integer id,
                                                          @Valid @RequestBody ProductoRequestDto request) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }
}