package com.abarrotes.oakapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CompraRequestDto {

    @NotNull(message = "El proveedor es obligatorio")
    private Integer proveedorId;

    @Size(max = 50, message = "El folio de factura no puede exceder 50 caracteres")
    private String folioFactura;

    private LocalDate fechaVencimientoPago;

    @NotEmpty(message = "La compra debe incluir al menos una línea de detalle")
    @Valid
    private List<DetalleCompraRequestDto> detalle;
}