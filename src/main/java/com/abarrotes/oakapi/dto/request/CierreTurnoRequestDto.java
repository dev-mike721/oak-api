package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CierreTurnoRequestDto {

    @NotNull(message = "El monto físico del conteo es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto físico no puede ser negativo")
    private BigDecimal montoFisicoConteo;
}