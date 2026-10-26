package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.MetodoPago;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConvertirPresupuestoRequestDto {

    @NotNull(message = "El método de pago es obligatorio")
    private MetodoPago metodoPago;

    private Integer clienteId;

    private Integer cajaId;
}