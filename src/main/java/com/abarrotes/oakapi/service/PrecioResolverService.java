package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.response.PrecioResueltoResponseDto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PrecioResolverService {

    PrecioResueltoResponseDto resolvePrecio(Integer clienteId, Integer productoId, java.math.BigDecimal cantidad, LocalDate fecha);
}