package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ListaPrecioDetalleRequestDto {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotNull(message = "El precio base es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio base debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio base debe tener máximo 8 enteros y 2 decimales")
    private BigDecimal precioBase;

    @Min(value = 1, message = "El volumen mínimo 1 debe ser mayor a cero")
    private Integer volMin1;

    @DecimalMin(value = "0.01", message = "El precio 1 debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio 1 debe tener máximo 8 enteros y 2 decimales")
    private BigDecimal precio1;

    @Min(value = 1, message = "El volumen mínimo 2 debe ser mayor a cero")
    private Integer volMin2;

    @DecimalMin(value = "0.01", message = "El precio 2 debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio 2 debe tener máximo 8 enteros y 2 decimales")
    private BigDecimal precio2;

    @Min(value = 1, message = "El volumen mínimo 3 debe ser mayor a cero")
    private Integer volMin3;

    @DecimalMin(value = "0.01", message = "El precio 3 debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio 3 debe tener máximo 8 enteros y 2 decimales")
    private BigDecimal precio3;
}