package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.TipoImpuesto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ProductoImpuestoResponseDto {

    private Integer productoId;
    private Integer impuestoId;
    private String impuestoClave;
    private String impuestoNombre;
    private BigDecimal tasaTrasladoEfectiva;
    private BigDecimal tasaRetencionEfectiva;
    private TipoImpuesto tipo;
    private BigDecimal tasaOverride;
    private LocalDateTime createdAt;
}