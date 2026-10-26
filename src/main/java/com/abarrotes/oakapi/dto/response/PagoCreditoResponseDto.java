package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.MetodoPagoCredito;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PagoCreditoResponseDto {

    private Integer id;
    private Integer clienteId;
    private String clienteNombre;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private MetodoPagoCredito metodoPago;
    private String referencia;
    private Boolean aplicado;
    private BigDecimal montoRestante;
    private Integer usuarioId;
    private String usuarioUsername;
    private Boolean anulado;
    private Integer anuladoPorId;
    private String anuladoPorUsername;
    private LocalDateTime anuladoEn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}