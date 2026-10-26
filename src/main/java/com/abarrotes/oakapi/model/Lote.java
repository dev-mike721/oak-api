package com.abarrotes.oakapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lotes",
       uniqueConstraints = @UniqueConstraint(name = "uq_lote_producto_codigo",
               columnNames = {"producto_id", "codigo_lote"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(name = "codigo_lote", nullable = false, length = 50)
    private String codigoLote;

    @Column(name = "costo_compra", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoCompra;

    @Column(name = "cantidad_inicial", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidadInicial;

    @Column(name = "cantidad_actual", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidadActual;

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDate fechaRecepcion;

    @Column(name = "fecha_caducidad")
    private LocalDate fechaCaducidad;
}
