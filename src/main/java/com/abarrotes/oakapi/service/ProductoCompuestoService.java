package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.ComponenteRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoCompuestoRequestDto;
import com.abarrotes.oakapi.dto.response.ProductoCompuestoResponseDto;

public interface ProductoCompuestoService {

    ProductoCompuestoResponseDto obtenerReceta(Integer productoPadreId);

    ProductoCompuestoResponseDto agregarComponente(Integer productoPadreId, ComponenteRequestDto request);

    ProductoCompuestoResponseDto reemplazarReceta(Integer productoPadreId, ProductoCompuestoRequestDto request);

    void eliminarComponente(Integer productoPadreId, Integer productoHijoId);

    void vaciarReceta(Integer productoPadreId);
}
