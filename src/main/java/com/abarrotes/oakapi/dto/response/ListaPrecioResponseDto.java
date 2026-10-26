package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ListaPrecioResponseDto {

    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFin;
    private Boolean activa;
    private Boolean esDefaultCanal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}