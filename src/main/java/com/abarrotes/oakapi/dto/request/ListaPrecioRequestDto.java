package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ListaPrecioRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 255, message = "La descripción no puede exceder 255 caracteres")
    private String descripcion;

    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFin;

    private Boolean activa = true;

    /** Si es true, esta lista es la de precios por defecto del canal (sólo una puede estarlo). */
    private Boolean esDefaultCanal = false;
}