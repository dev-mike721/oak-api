package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoImpuestoRequestDto {

    @NotNull(message = "El impuesto es obligatorio")
    private Integer impuestoId;

    @Digits(integer = 1, fraction = 4, message = "La tasa override debe tener 1 entero y 4 decimales")
    private BigDecimal tasaOverride;
}