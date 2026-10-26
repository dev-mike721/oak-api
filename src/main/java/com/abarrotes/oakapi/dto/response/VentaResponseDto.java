package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class VentaResponseDto {
    private String uuid;
    private Integer turnoId;
    private Integer usuarioId;
    private String usuarioUsername;
    private Integer clienteId;
    private String clienteNombre;
    private String clienteRfc;
    private String presupuestoId;
    private Integer cajaId;
    private String cajaNombre;
    private LocalDateTime fechaHora;
    private MetodoPago metodoPago;
    private BigDecimal total;
    private BigDecimal saldoPendiente;
    private LocalDate fechaVencimiento;
    private EstadoVenta estado;
    private Boolean sincronizado;
    private List<DetalleVentaResponseDto> detalle;
}