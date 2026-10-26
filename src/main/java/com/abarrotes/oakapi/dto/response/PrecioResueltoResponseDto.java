package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PrecioResueltoResponseDto {

    private BigDecimal precioUnitario;
    private String origen; // ESPECIAL, LISTA_CLIENTE, LISTA_CANAL, BASE
    private Integer listaPrecioId;
    private String listaPrecioNombre;
    private Integer escalonAplicado; // 0 = base, 1, 2, 3
}