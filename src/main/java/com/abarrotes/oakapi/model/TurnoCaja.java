package com.abarrotes.oakapi.model;

import com.abarrotes.oakapi.model.enums.EstadoTurno;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Turnos_Caja")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = "usuario")
public class TurnoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_apertura", nullable = false, updatable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "monto_inicial", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "monto_fisico_conteo", precision = 10, scale = 2)
    private BigDecimal montoFisicoConteo;

    @Column(name = "monto_sistema_calculado", precision = 10, scale = 2)
    private BigDecimal montoSistemaCalculado;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;
}