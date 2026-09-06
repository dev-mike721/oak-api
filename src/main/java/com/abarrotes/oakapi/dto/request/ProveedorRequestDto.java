package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProveedorRequestDto {

    @NotBlank(message = "La razón social es obligatoria")
    @Size(max = 120, message = "La razón social no puede exceder 120 caracteres")
    private String razonSocial;

    @Size(max = 20, message = "El RFC no puede exceder 20 caracteres")
    private String rfc;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    @Email(message = "El correo no es válido")
    @Size(max = 100, message = "El correo no puede exceder 100 caracteres")
    private String email;

    private String direccion;

    @NotNull(message = "Los días de crédito son obligatorios")
    @Min(value = 0, message = "Los días de crédito no pueden ser negativos")
    @Max(value = 365, message = "Los días de crédito no pueden exceder 365")
    private Integer diasCredito;
}