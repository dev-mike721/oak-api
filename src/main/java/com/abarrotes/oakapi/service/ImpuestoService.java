package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.ImpuestoRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoImpuestoRequestDto;
import com.abarrotes.oakapi.dto.response.ImpuestoResponseDto;
import com.abarrotes.oakapi.dto.response.ProductoImpuestoResponseDto;

import java.util.List;

public interface ImpuestoService {

    List<ImpuestoResponseDto> obtenerTodos();

    ImpuestoResponseDto obtenerPorId(Integer id);

    ImpuestoResponseDto crear(com.abarrotes.oakapi.dto.request.ImpuestoRequestDto request);

    ImpuestoResponseDto actualizar(Integer id, com.abarrotes.oakapi.dto.request.ImpuestoRequestDto request);

    void eliminar(Integer id);

    // Producto-Impuesto
    ProductoImpuestoResponseDto asignarImpuestoAProducto(Integer productoId, com.abarrotes.oakapi.dto.request.ProductoImpuestoRequestDto request);

    List<ProductoImpuestoResponseDto> obtenerImpuestosDeProducto(Integer productoId);

    void quitarImpuestoDeProducto(Integer productoId, Integer impuestoId);
}