package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.EstadoSerie;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SerieResponseDto {

    private Integer id;
    private Integer productoId;
    private String productoNombre;
    private String productoPlu;
    private Integer loteId;
    private String loteCodigo;
    private String codigoSerie;
    private EstadoSerie estado;
    private String ventaId;
    private LocalDateTime fechaVenta;
    private LocalDateTime fechaGarantia;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}