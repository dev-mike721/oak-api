package com.abarrotes.oakapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "producto_impuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"producto", "impuesto"})
@ToString
public class ProductoImpuesto {

    @EmbeddedId
    private ProductoImpuestoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("impuestoId")
    @JoinColumn(name = "impuesto_id", nullable = false)
    @ToString.Exclude
    private Impuesto impuesto;

    @Column(name = "tasa_override", precision = 5, scale = 4)
    private BigDecimal tasaOverride;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}