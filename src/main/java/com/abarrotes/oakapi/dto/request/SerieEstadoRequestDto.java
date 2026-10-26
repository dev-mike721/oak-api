package com.abarrotes.oakapi.dto.request;

import com.abarrotes.oakapi.model.enums.EstadoSerie;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SerieEstadoRequestDto {

    @NotNull(message = "El estado es obligatorio")
    private EstadoSerie estado;
}