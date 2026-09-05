package com.abarrotes.oakapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Productos_Compuestos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class ProductoCompuesto {

    @EmbeddedId
    private ProductoCompuestoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoPadreId")
    @JoinColumn(name = "producto_padre_id", nullable = false)
    private Producto productoPadre;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productoHijoId")
    @JoinColumn(name = "producto_hijo_id", nullable = false)
    private Producto productoHijo;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;
}
