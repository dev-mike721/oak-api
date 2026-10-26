package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.ProductoImagenRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoImagenUploadRequest;
import com.abarrotes.oakapi.dto.response.ProductoImagenResponseDto;
import com.abarrotes.oakapi.dto.response.ProductoImagenUploadResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface ProductoImagenService {

    ProductoImagenResponseDto actualizarImagen(Integer productoId, ProductoImagenRequestDto request);

    ProductoImagenResponseDto obtenerImagen(Integer productoId);

    void eliminarImagen(Integer productoId);

    // Nuevos métodos para subida de archivos
    ProductoImagenUploadResponseDto subirImagen(Integer productoId, org.springframework.web.multipart.MultipartFile archivo);

    ProductoImagenUploadResponseDto actualizarImagenArchivo(Integer productoId, org.springframework.web.multipart.MultipartFile archivo);

    org.springframework.core.io.Resource obtenerImagenArchivo(Integer productoId);

    String obtenerContentTypeImagenArchivo(Integer productoId);

    String obtenerNombreImagenArchivo(Integer productoId);

    void eliminarImagenArchivo(Integer productoId);
}
