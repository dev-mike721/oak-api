package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresupuestoRequestDto {
    @NotNull(message = "El cliente es obligatorio")
    private Integer clienteId;
    @NotEmpty(message = "El presupuesto debe tener al menos una línea")
    private List<PresupuestoDetalleRequestDto> lineas;
    @Min(value = 1, message = "La vigencia debe ser al menos 1 día")
    @Max(value = 365, message = "La vigencia no puede exceder 365 días")
    private Integer vigenciaDias = 30;
    private String observaciones;
}
