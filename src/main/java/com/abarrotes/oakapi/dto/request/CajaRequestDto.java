package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CajaRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder 50 caracteres")
    private String nombre;

    @Size(max = 100, message = "La ubicación no puede exceder 100 caracteres")
    private String ubicacion;

    private Boolean activa = true;
}