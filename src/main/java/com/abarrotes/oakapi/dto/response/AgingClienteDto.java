package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AgingClienteDto {

    private Integer clienteId;
    private String clienteNombre;
    private String clienteRfc;
    private BigDecimal corriente;
    private BigDecimal dias31a60;
    private BigDecimal dias61a90;
    private BigDecimal dias91a120;
    private BigDecimal mas120;
    private BigDecimal total;
}