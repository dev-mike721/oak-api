package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ClientePrecioEspecialRequestDto {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio debe tener máximo 8 enteros y 2 decimales")
    private BigDecimal precio;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFin;
}