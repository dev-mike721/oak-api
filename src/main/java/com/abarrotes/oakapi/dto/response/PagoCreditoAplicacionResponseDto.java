package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagoCreditoAplicacionResponseDto {

    private Integer pagoId;
    private String ventaId;
    private BigDecimal montoAplicado;
    private LocalDateTime fechaAplicacion;
    private String ventaFolio;
    private BigDecimal ventaTotal;
    private BigDecimal ventaSaldoAnterior;
    private BigDecimal ventaSaldoPosterior;
}