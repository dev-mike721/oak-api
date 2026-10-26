package com.abarrotes.oakapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lista_precio_detalle")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"listaPrecio", "producto"})
@ToString
public class ListaPrecioDetalle {

    @EmbeddedId
    private ListaPrecioDetalleId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("listaPrecioId")
    @JoinColumn(name = "lista_precio_id", nullable = false)
    @ToString.Exclude
    private ListaPrecio listaPrecio;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoId")
    @JoinColumn(name = "producto_id", nullable = false)
    @ToString.Exclude
    private Producto producto;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(name = "vol_min_1")
    private Integer volMin1;

    @Column(name = "precio_1", precision = 10, scale = 2)
    private BigDecimal precio1;

    @Column(name = "vol_min_2")
    private Integer volMin2;

    @Column(name = "precio_2", precision = 10, scale = 2)
    private BigDecimal precio2;

    @Column(name = "vol_min_3")
    private Integer volMin3;

    @Column(name = "precio_3", precision = 10, scale = 2)
    private BigDecimal precio3;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}