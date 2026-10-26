package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AjusteInventarioRequestDto {

    @NotNull(message = "El lote es obligatorio")
    private Integer loteId;

    @NotNull(message = "El tipo de movimiento es obligatorio")
    private TipoMovimientoInventario tipoMovimiento;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private BigDecimal cantidad;

    @NotBlank(message = "El motivo es obligatorio para ajustes y mermas")
    @Size(max = 150, message = "El motivo no puede exceder 150 caracteres")
    private String motivo;
}
