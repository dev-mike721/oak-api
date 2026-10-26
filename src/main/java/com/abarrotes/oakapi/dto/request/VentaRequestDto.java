package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.MetodoPago;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class VentaRequestDto {

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    private Integer clienteId;

    private String presupuestoId;

    private Integer cajaId;

    @NotEmpty(message = "La venta debe incluir al menos una línea")
    @Valid
    private List<LineaVentaRequestDto> detalle;
}