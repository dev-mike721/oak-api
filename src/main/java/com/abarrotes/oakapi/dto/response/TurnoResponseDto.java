package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.EstadoSolicitudTurno;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TurnoResponseDto {
    private Integer id;
    private Integer usuarioId;
    private String usuarioUsername;
    private Integer cajaId;
    private String cajaNombre;
    private LocalDateTime fechaApertura;
    private BigDecimal montoInicial;
    private LocalDateTime fechaCierre;
    private BigDecimal montoFisicoConteo;
    private BigDecimal montoSistemaCalculado;
    private BigDecimal diferencia;
    private EstadoTurno estado;
    private EstadoSolicitudTurno estadoSolicitud;
    private Integer solicitadoPorId;
    private String solicitadoPorUsername;
    private Integer autorizadoPorId;
    private String autorizadoPorUsername;
    private LocalDateTime autorizadoEn;
    private String motivoRechazo;
}