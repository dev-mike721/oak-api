package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.TipoImpuesto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ImpuestoResponseDto {

    private Integer id;
    private String clave;
    private String nombre;
    private BigDecimal tasaTraslado;
    private BigDecimal tasaRetencion;
    private TipoImpuesto tipo;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}