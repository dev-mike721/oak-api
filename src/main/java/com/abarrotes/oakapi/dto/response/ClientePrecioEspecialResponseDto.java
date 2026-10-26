package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ClientePrecioEspecialResponseDto {

    private Integer clienteId;
    private Integer productoId;
    private String productoNombre;
    private String productoPlu;
    private BigDecimal precio;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFin;
    private LocalDateTime createdAt;
}