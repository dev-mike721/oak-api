package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProveedorResponseDto {
    private Integer id;
    private String razonSocial;
    private String rfc;
    private String telefono;
    private String email;
    private String direccion;
    private Integer diasCredito;
    private Boolean activo;
    private LocalDateTime fechaRegistro;
}