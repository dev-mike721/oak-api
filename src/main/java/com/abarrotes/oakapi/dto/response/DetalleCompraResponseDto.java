package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DetalleCompraResponseDto {
    private Long id;
    private Integer productoId;
    private String productoNombre;
    private String plu;
    private Integer loteId;
    private String codigoLote;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;
    private LocalDate fechaCaducidad;
}