package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SolicitarTurnoRequestDto {

    @NotNull(message = "La caja es obligatoria")
    private Integer cajaId;

    @NotNull(message = "El monto inicial es obligatorio")
    @DecimalMin(value = "0.00", message = "El monto inicial no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El monto inicial debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal montoInicial;
}