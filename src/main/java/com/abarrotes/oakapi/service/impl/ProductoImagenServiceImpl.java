package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.ProductoImagenRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoImagenUploadRequest;
import com.abarrotes.oakapi.dto.response.ProductoImagenResponseDto;
import com.abarrotes.oakapi.dto.response.ProductoImagenUploadResponseDto;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.exception.StorageException;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.ImagenStorageService;
import com.abarrotes.oakapi.service.ProductoImagenService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProductoImagenServiceImpl implements ProductoImagenService {

    private final ProductoRepository productoRepository;
    private final ImagenStorageService imagenStorageService;

    private static final String PUBLIC_URL_TEMPLATE = "/api/v1/productos/%d/foto/archivo";

    @Override
    @Transactional
    public ProductoImagenResponseDto actualizarImagen(Integer productoId, ProductoImagenRequestDto request) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        producto.setImagenUrl(request.getImagenUrl());
        productoRepository.save(producto);

        return ProductoImagenResponseDto.builder()
                .productoId(producto.getId())
                .imagenUrl(producto.getImagenUrl())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoImagenResponseDto obtenerImagen(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        return ProductoImagenResponseDto.builder()
                .productoId(producto.getId())
                .imagenUrl(producto.getImagenUrl())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }

    @Override
    @Transactional
    public void eliminarImagen(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        producto.setImagenUrl(null);
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public ProductoImagenUploadResponseDto subirImagen(Integer productoId, MultipartFile archivo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        String rutaAnterior = producto.getImagenStorageKey();
        String rutaRelativa = imagenStorageService.guardarImagen(archivo, producto.getPlu());

        if (rutaAnterior != null && !rutaAnterior.isEmpty() && !rutaAnterior.equals(rutaRelativa)) {
            imagenStorageService.eliminarImagen(rutaAnterior);
        }

        producto.setImagenUrl(String.format(PUBLIC_URL_TEMPLATE, productoId));
        producto.setImagenStorageKey(rutaRelativa);
        producto.setImagenContentType(imagenStorageService.getContentType(rutaRelativa));
        producto.setImagenNombreOriginal(archivo.getOriginalFilename());
        producto.setImagenSizeBytes(archivo.getSize());
        producto.setImagenUpdatedAt(LocalDateTime.now());
        productoRepository.save(producto);

        return buildUploadResponse(producto, archivo);
    }

    @Override
    @Transactional
    public ProductoImagenUploadResponseDto actualizarImagenArchivo(Integer productoId, MultipartFile archivo) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        if (producto.getImagenStorageKey() != null && !producto.getImagenStorageKey().isEmpty()) {
            imagenStorageService.eliminarImagen(producto.getImagenStorageKey());
        }

        String rutaRelativa = imagenStorageService.guardarImagen(archivo, producto.getPlu());

        producto.setImagenUrl(String.format(PUBLIC_URL_TEMPLATE, productoId));
        producto.setImagenStorageKey(rutaRelativa);
        producto.setImagenContentType(imagenStorageService.getContentType(rutaRelativa));
        producto.setImagenNombreOriginal(archivo.getOriginalFilename());
        producto.setImagenSizeBytes(archivo.getSize());
        producto.setImagenUpdatedAt(LocalDateTime.now());
        productoRepository.save(producto);

        return buildUploadResponse(producto, archivo);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource obtenerImagenArchivo(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        String rutaRelativa = producto.getImagenStorageKey();
        if (rutaRelativa == null || rutaRelativa.isEmpty()) {
            return null;
        }
        return imagenStorageService.obtenerImagen(rutaRelativa);
    }

    @Override
    public String obtenerContentTypeImagenArchivo(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        String rutaRelativa = producto.getImagenStorageKey();
        if (rutaRelativa == null || rutaRelativa.isEmpty()) {
            return "application/octet-stream";
        }
        return imagenStorageService.getContentType(rutaRelativa);
    }

    @Override
    public String obtenerNombreImagenArchivo(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        String nombreOriginal = producto.getImagenNombreOriginal();
        if (nombreOriginal != null && !nombreOriginal.isEmpty()) {
            return nombreOriginal;
        }

        String rutaRelativa = producto.getImagenStorageKey();
        if (rutaRelativa == null || rutaRelativa.isEmpty()) {
            return "imagen.jpg";
        }
        String[] partes = rutaRelativa.split("/");
        return partes[partes.length - 1];
    }

    @Override
    @Transactional
    public void eliminarImagenArchivo(Integer productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        String rutaRelativa = producto.getImagenStorageKey();
        if (rutaRelativa != null && !rutaRelativa.isEmpty()) {
            imagenStorageService.eliminarImagen(rutaRelativa);
        }

        producto.setImagenUrl(null);
        producto.setImagenStorageKey(null);
        producto.setImagenContentType(null);
        producto.setImagenNombreOriginal(null);
        producto.setImagenSizeBytes(null);
        producto.setImagenUpdatedAt(null);
        productoRepository.save(producto);
    }

    private ProductoImagenUploadResponseDto buildUploadResponse(Producto producto, MultipartFile archivo) {
        return ProductoImagenUploadResponseDto.builder()
                .productoId(producto.getId())
                .imagenUrl(producto.getImagenUrl())
                .nombreOriginal(archivo.getOriginalFilename())
                .tamañoBytes(archivo.getSize())
                .contentType(producto.getImagenContentType())
                .fechaActualizacion(LocalDateTime.now())
                .build();
    }
}