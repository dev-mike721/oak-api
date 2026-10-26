package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VentaPendienteDto {

    private String uuid;
    private LocalDateTime fecha;
    private LocalDate fechaVencimiento;
    private BigDecimal total;
    private BigDecimal saldoPendiente;
    private Integer diasVencido;
}