package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class SeriesDisponiblesResponseDto {

    private Integer id;
    private String codigoSerie;
    private Integer loteId;
    private String loteCodigo;
    private LocalDate fechaRecepcion;
}