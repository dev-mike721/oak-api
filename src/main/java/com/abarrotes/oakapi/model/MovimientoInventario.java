package com.abarrotes.oakapi.model;

import com.abarrotes.oakapi.model.enums.DocumentoOrigen;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimiento", nullable = false)
    private TipoMovimientoInventario tipoMovimiento;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "cantidad_anterior", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidadAnterior;

    @Column(name = "cantidad_nueva", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidadNueva;

    @Column(length = 150)
    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(name = "documento_tipo")
    private DocumentoOrigen documentoTipo;

    @Column(name = "documento_id", length = 36)
    private String documentoId;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime fechaHora = LocalDateTime.now();
}
