package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.dto.response.AplicacionResumenDto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PagoResumenDto {

    private Integer id;
    private LocalDateTime fecha;
    private BigDecimal monto;
    private Boolean aplicado;
    private BigDecimal montoRestante;
    private List<AplicacionResumenDto> aplicaciones;
}