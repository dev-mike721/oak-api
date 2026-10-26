package com.abarrotes.oakapi.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pago_credito_aplicacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = {"pago", "venta"})
@ToString
public class PagoCreditoAplicacion {

    @EmbeddedId
    private PagoCreditoAplicacionId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("pagoId")
    @JoinColumn(name = "pago_id", nullable = false)
    @ToString.Exclude
    private PagoCredito pago;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ventaId")
    @JoinColumn(name = "venta_id", nullable = false, columnDefinition = "CHAR(36)", referencedColumnName = "uuid")
    @PrimaryKeyJoinColumn(name = "venta_id", referencedColumnName = "uuid")
    @ToString.Exclude
    private Venta venta;

    @Column(name = "monto_aplicado", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoAplicado;

    @Column(name = "fecha_aplicacion", nullable = false)
    @Builder.Default
    private LocalDateTime fechaAplicacion = LocalDateTime.now();
}