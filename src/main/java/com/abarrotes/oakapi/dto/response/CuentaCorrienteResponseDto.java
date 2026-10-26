package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CuentaCorrienteResponseDto {

    private Integer clienteId;
    private String clienteNombre;
    private BigDecimal saldoTotal;
    private List<VentaPendienteDto> ventasPendientes;
    private List<PagoResumenDto> pagos;
    private BigDecimal totalVentasPendientes;
    private BigDecimal totalPagosAplicados;
    private BigDecimal totalPagosPendientes;
}