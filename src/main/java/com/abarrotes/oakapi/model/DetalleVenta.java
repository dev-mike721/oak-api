package com.abarrotes.oakapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "Detalle_Ventas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"venta", "producto", "lote"})
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "venta_uuid", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario_aplicado", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitarioAplicado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
}