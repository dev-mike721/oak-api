package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ListaPrecioDetalleResponseDto {

    private Integer listaPrecioId;
    private Integer productoId;
    private String productoNombre;
    private String productoPlu;
    private BigDecimal precioBase;
    private Integer volMin1;
    private BigDecimal precio1;
    private Integer volMin2;
    private BigDecimal precio2;
    private Integer volMin3;
    private BigDecimal precio3;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}