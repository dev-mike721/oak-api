package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.UnidadMedida;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductoRequestDto {

    @NotBlank(message = "El PLU es obligatorio")
    private String plu;

    @NotBlank(message = "El nombre del producto es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private Integer categoriaId;

    @NotNull(message = "La unidad de medida es obligatoria")
    private UnidadMedida unidadMedida;

    private Boolean permiteDecimales;

    @NotNull(message = "El precio base es obligatorio")
    @PositiveOrZero(message = "El precio base no puede ser negativo")
    private BigDecimal precioBase;

    private Boolean esCompuesto;
}