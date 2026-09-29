package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LineaVentaRequestDto {

    @NotNull(message = "El producto es obligatorio en cada línea")
    private Integer productoId;

    @NotNull(message = "La cantidad es obligatoria en cada línea")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;
}