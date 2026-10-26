package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoriaDto {
    private Integer id;
    private String nombre;
    private String descripcion;
}