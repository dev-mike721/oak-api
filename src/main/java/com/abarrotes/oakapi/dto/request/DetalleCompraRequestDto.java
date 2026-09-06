package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DetalleCompraRequestDto {

    @NotNull(message = "El producto es obligatorio en cada línea")
    private Integer productoId;

    @NotNull(message = "La cantidad es obligatoria en cada línea")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotNull(message = "El costo unitario es obligatorio en cada línea")
    @DecimalMin(value = "0.00", message = "El costo unitario no puede ser negativo")
    private BigDecimal costoUnitario;

    @FutureOrPresent(message = "La fecha de caducidad no puede ser anterior a hoy")
    private LocalDate fechaCaducidad;

    @Size(max = 50, message = "El código de lote no puede exceder 50 caracteres")
    private String codigoLote;
}