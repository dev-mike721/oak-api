package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import jakarta.validation.constraints.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDto {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
    private String nombre;

    @Size(max = 150, message = "La razón social no puede exceder 150 caracteres")
    private String razonSocial;

    @NotBlank(message = "El RFC es obligatorio")
    @Size(min = 12, max = 13, message = "El RFC debe tener 12 o 13 caracteres")
    private String rfc;

    @NotNull(message = "El régimen fiscal es obligatorio")
    private RegimenFiscal regimenFiscal;

    @Email(message = "El email debe tener un formato válido")
    @Size(max = 100, message = "El email no puede exceder 100 caracteres")
    private String email;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;

    private String direccion;

    @DecimalMin(value = "0.00", message = "El límite de crédito no puede ser negativo")
    @Digits(integer = 10, fraction = 2, message = "El límite de crédito debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Min(value = 0, message = "Los días de crédito no pueden ser negativos")
    @Max(value = 365, message = "Los días de crédito no pueden exceder 365")
    private Integer diasCredito = 0;

    private Integer listaPrecioId;
}