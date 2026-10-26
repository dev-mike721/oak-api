package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class AlertaCaducidadDto {
    private Integer loteId;
    private String codigoLote;
    private Integer productoId;
    private String plu;
    private String productoNombre;
    private BigDecimal cantidadActual;
    private BigDecimal costoCompra;
    private LocalDate fechaCaducidad;
    private Long diasRestantes;
    private NivelCaducidad nivel;
    private BigDecimal valorEnRiesgo;

    public enum NivelCaducidad {
        ROJO, AMARILLO, VERDE
    }
}
