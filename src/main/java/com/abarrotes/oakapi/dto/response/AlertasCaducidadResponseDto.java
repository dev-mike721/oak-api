package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AlertasCaducidadResponseDto {
    private Integer diasConsulta;
    private Long totalAlertas;
    private Long enRojo;
    private Long enAmarillo;
    private BigDecimal valorEnRiesgoTotal;
    private List<AlertaCaducidadDto> alertas;
}
