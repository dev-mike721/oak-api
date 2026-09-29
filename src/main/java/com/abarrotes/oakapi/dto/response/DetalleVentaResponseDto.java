package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DetalleVentaResponseDto {
    private Long id;
    private Integer productoId;
    private String productoNombre;
    private String plu;
    private Boolean esCompuesto;
    private Integer loteId;
    private String codigoLote;
    private BigDecimal cantidad;
    private BigDecimal precioUnitarioAplicado;
    private BigDecimal subtotal;
}