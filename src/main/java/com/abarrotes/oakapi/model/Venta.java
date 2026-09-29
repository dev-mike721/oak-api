package com.abarrotes.oakapi.model;

import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "uuid")
@ToString(exclude = {"turno", "usuario", "detalle"})
public class Venta {

    @Id
    @Column(length = 36, columnDefinition = "CHAR(36)")
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "turno_id", nullable = false)
    private TurnoCaja turno;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "metodo_pago", nullable = false)
    private MetodoPago metodoPago;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado;

    @Builder.Default
    @Column(nullable = false)
    private Boolean sincronizado = false;

    @Builder.Default
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL)
    private List<DetalleVenta> detalle = new ArrayList<>();
}