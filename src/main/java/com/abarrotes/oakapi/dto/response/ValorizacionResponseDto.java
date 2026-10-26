package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ValorizacionResponseDto {
    private BigDecimal valorTotal;
    private Long totalLotes;
    private List<ValorPorCategoriaDto> detalle;

    @Data
    @Builder
    public static class ValorPorCategoriaDto {
        private String categoria;
        private BigDecimal valor;
        private Long lotes;
    }
}
