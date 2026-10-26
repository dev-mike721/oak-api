package com.abarrotes.oakapi.model;

import com.abarrotes.oakapi.model.enums.TipoImpuesto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "impuesto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Impuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clave", nullable = false, unique = true, length = 20)
    private String clave;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tasa_traslado", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal tasaTraslado = BigDecimal.ZERO;

    @Column(name = "tasa_retencion", nullable = false, precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal tasaRetencion = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false)
    @Builder.Default
    private TipoImpuesto tipo = TipoImpuesto.TRASLADO;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "impuesto", cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ProductoImpuesto> productos = new ArrayList<>();

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