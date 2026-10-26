package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AplicacionResumenDto {

    private String ventaId;
    private BigDecimal montoAplicado;
}