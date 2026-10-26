package com.abarrotes.oakapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "detalle_venta_impuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class DetalleVentaImpuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "detalle_venta_id", nullable = false)
    @ToString.Exclude
    private DetalleVenta detalleVenta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "impuesto_id", nullable = false)
    @ToString.Exclude
    private Impuesto impuesto;

    @Column(name = "base", nullable = false, precision = 12, scale = 2)
    private BigDecimal base;

    @Column(name = "tasa", nullable = false, precision = 5, scale = 4)
    private BigDecimal tasa;

    @Column(name = "importe_traslado", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal importeTraslado = BigDecimal.ZERO;

    @Column(name = "importe_retencion", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal importeRetencion = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}