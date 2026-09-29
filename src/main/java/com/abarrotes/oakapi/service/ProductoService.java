package com.abarrotes.oakapi.service;
import com.abarrotes.oakapi.dto.request.ProductoRequestDto;
import com.abarrotes.oakapi.dto.response.ProductoResponseDto;
import java.util.List;

public interface ProductoService {
    List<ProductoResponseDto> obtenerTodos();
    ProductoResponseDto buscarPorId(Integer id);
    ProductoResponseDto buscarPorPlu(String plu);
    List<ProductoResponseDto> buscarPorCategoria(Integer categoriaId);
    List<ProductoResponseDto> buscarPorNombre(String nombre);
    ProductoResponseDto crearProducto(ProductoRequestDto request);
    ProductoResponseDto actualizarProducto(Integer id, ProductoRequestDto request);
    void eliminarProducto(Integer id);
    List<ProductoResponseDto> obtenerInactivos();
    ProductoResponseDto reactivarProducto(Integer id);
}
