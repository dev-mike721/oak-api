package com.abarrotes.oakapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductoCompuestoRequestDto {

    @NotNull(message = "El producto compuesto es obligatorio")
    private Integer productoPadreId;

    @NotEmpty(message = "La receta debe tener al menos un componente")
    @Valid
    private List<ComponenteRequestDto> componentes;
}
