package com.abarrotes.oakapi.controller;

import com.abarrotes.oakapi.dto.request.ProductoImagenRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoImagenUploadRequest;
import com.abarrotes.oakapi.dto.response.ProductoImagenResponseDto;
import com.abarrotes.oakapi.dto.response.ProductoImagenUploadResponseDto;
import com.abarrotes.oakapi.service.ProductoImagenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/productos/{productoId}/foto")
@RequiredArgsConstructor
public class ProductoImagenController {

    private final ProductoImagenService productoImagenService;

    @PutMapping
    public ResponseEntity<ProductoImagenResponseDto> actualizarImagen(
            @PathVariable Integer productoId,
            @Valid @RequestBody ProductoImagenRequestDto request) {
        ProductoImagenResponseDto response = productoImagenService.actualizarImagen(productoId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ProductoImagenResponseDto> obtenerImagen(@PathVariable Integer productoId) {
        ProductoImagenResponseDto response = productoImagenService.obtenerImagen(productoId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminarImagen(@PathVariable Integer productoId) {
        productoImagenService.eliminarImagen(productoId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoImagenUploadResponseDto> subirImagen(
            @PathVariable Integer productoId,
            @RequestParam("archivo") MultipartFile archivo) {
        ProductoImagenUploadResponseDto response = productoImagenService.subirImagen(productoId, archivo);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping(value = "/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductoImagenUploadResponseDto> actualizarImagenArchivo(
            @PathVariable Integer productoId,
            @RequestParam("archivo") MultipartFile archivo) {
        ProductoImagenUploadResponseDto response = productoImagenService.actualizarImagenArchivo(productoId, archivo);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/archivo", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<org.springframework.core.io.Resource> obtenerImagenArchivo(@PathVariable Integer productoId) {
        org.springframework.core.io.Resource resource = productoImagenService.obtenerImagenArchivo(productoId);
        if (resource == null) {
            return ResponseEntity.notFound().build();
        }
        String contentType = productoImagenService.obtenerContentTypeImagenArchivo(productoId);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                .header("Content-Disposition", "inline; filename=\"" + productoImagenService.obtenerNombreImagenArchivo(productoId) + "\"")
                .body(resource);
    }

    @DeleteMapping("/archivo")
    public ResponseEntity<Void> eliminarImagenArchivo(@PathVariable Integer productoId) {
        productoImagenService.eliminarImagenArchivo(productoId);
        return ResponseEntity.noContent().build();
    }
}
