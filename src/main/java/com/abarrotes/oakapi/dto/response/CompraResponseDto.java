package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.EstadoCompra;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CompraResponseDto {
    private Integer id;
    private Integer proveedorId;
    private String proveedorRazonSocial;
    private Integer usuarioId;
    private String usuarioUsername;
    private String folioFactura;
    private LocalDateTime fechaCompra;
    private LocalDate fechaVencimientoPago;
    private BigDecimal total;
    private EstadoCompra estado;
    private List<DetalleCompraResponseDto> detalle;
}