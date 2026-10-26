package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PresupuestoDetalleResponseDto {

    private Integer id;
    private String presupuestoId;
    private Integer productoId;
    private String productoNombre;
    private String productoPlu;
    private BigDecimal cantidad;
    private BigDecimal precioUnitario;
    private Integer impuestoId;
    private String impuestoClave;
    private BigDecimal impuestoTasa;
    private BigDecimal impuestoImporte;
    private BigDecimal subtotal;
    private BigDecimal total;
    private LocalDateTime createdAt;
}