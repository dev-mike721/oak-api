package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class AgingResponseDto {

    private BigDecimal totalCorriente;
    private BigDecimal total31a60;
    private BigDecimal total61a90;
    private BigDecimal total91a120;
    private BigDecimal totalMas120;
    private BigDecimal totalGeneral;
    private Integer clientesConDeuda;
    private List<AgingClienteDto> detallePorCliente;
}