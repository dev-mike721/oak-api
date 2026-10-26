package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ClienteResponseDto {

    private Integer id;
    private String nombre;
    private String razonSocial;
    private String rfc;
    private RegimenFiscal regimenFiscal;
    private String email;
    private String telefono;
    private String direccion;
    private BigDecimal limiteCredito;
    private Integer diasCredito;
    private BigDecimal saldoPendiente;
    private Integer listaPrecioId;
    private String listaPrecioNombre;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}