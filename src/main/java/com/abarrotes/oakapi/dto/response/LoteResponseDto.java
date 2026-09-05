package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoteResponseDto {
    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private String codigoLote;
    private BigDecimal costoCompra;
    private BigDecimal cantidadInicial;
    private BigDecimal cantidadActual;
    private LocalDate fechaRecepcion;
    private LocalDate fechaCaducidad;
    private BigDecimal valorInventario;
}
