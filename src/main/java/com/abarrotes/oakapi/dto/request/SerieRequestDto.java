package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class SerieRequestDto {

    @NotNull(message = "El producto es obligatorio")
    private Integer productoId;

    @NotEmpty(message = "Debe proporcionar al menos un código de serie")
    @Size(max = 1000, message = "No puede exceder 1000 series por lote")
    private List<@NotBlank @Size(max = 100) String> series;

    private Integer loteId;
}