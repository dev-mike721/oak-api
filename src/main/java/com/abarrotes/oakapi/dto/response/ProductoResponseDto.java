package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.UnidadMedida;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductoResponseDto {
    private Integer id;
    private String plu;
    private String nombre;
    private String descripcion;
    private Integer categoriaId;
    private String categoriaNombre;
    private UnidadMedida unidadMedida;
    private Boolean permiteDecimales;
    private BigDecimal precioBase;
    private Boolean esCompuesto;
    private Boolean activo;
}