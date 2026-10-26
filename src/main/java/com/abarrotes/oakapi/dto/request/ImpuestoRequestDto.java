package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.TipoImpuesto;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ImpuestoRequestDto {

    @NotBlank(message = "La clave es obligatoria")
    @Size(max = 20, message = "La clave no puede exceder 20 caracteres")
    private String clave;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @DecimalMin(value = "0.0000", message = "La tasa de traslado no puede ser negativa")
    @Digits(integer = 1, fraction = 4, message = "La tasa de traslado debe tener 1 entero y 4 decimales")
    private BigDecimal tasaTraslado = BigDecimal.ZERO;

    @DecimalMin(value = "0.0000", message = "La tasa de retención no puede ser negativa")
    @Digits(integer = 1, fraction = 4, message = "La tasa de retención debe tener 1 entero y 4 decimales")
    private BigDecimal tasaRetencion = BigDecimal.ZERO;

    @NotNull(message = "El tipo de impuesto es obligatorio")
    private TipoImpuesto tipo;

    private Boolean activo = true;
}