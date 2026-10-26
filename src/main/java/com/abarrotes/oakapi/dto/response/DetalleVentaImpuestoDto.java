package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DetalleVentaImpuestoDto {
    private Integer impuestoId;
    private String impuestoClave;
    private String impuestoNombre;
    private BigDecimal base;
    private BigDecimal tasa;
    private BigDecimal importeTraslado;
    private BigDecimal importeRetencion;
}