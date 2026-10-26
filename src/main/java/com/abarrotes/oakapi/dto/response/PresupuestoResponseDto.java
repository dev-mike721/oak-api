package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PresupuestoResponseDto {

    private String id;
    private Integer clienteId;
    private String clienteNombre;
    private Integer usuarioId;
    private String usuarioUsername;
    private LocalDate fecha;
    private Integer vigenciaDias;
    private LocalDate fechaVigencia;
    private EstadoPresupuesto estado;
    private BigDecimal subtotal;
    private BigDecimal impuestosTotal;
    private BigDecimal total;
    private String observaciones;
    private Integer aprobadoPor;
    private String aprobadoPorUsername;
    private LocalDateTime aprobadoEn;
    private String ventaUuid;
    private List<PresupuestoDetalleResponseDto> lineas;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}