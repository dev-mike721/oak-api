package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.DocumentoOrigen;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MovimientoInventarioResponseDto {
    private Long id;
    private Integer loteId;
    private String codigoLote;
    private Integer productoId;
    private String productoNombre;
    private String username;
    private TipoMovimientoInventario tipoMovimiento;
    private BigDecimal cantidad;
    private BigDecimal cantidadAnterior;
    private BigDecimal cantidadNueva;
    private String motivo;
    private DocumentoOrigen documentoTipo;
    private String documentoId;
    private LocalDateTime fechaHora;
}
