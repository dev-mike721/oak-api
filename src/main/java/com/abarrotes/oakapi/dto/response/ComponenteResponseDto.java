package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.UnidadMedida;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ComponenteResponseDto {
    private Integer productoHijoId;
    private String plu;
    private String nombre;
    private UnidadMedida unidadMedida;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
