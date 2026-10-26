package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductoCompuestoResponseDto {
    private Integer productoPadreId;
    private String plu;
    private String nombre;
    private List<ComponenteResponseDto> componentes;
    private BigDecimal precioSugerido;
}
