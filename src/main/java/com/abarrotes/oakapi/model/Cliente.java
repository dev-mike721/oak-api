package com.abarrotes.oakapi.model;

import com.abarrotes.oakapi.converter.RegimenFiscalConverter;
import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import jakarta.persistence.*;
import jakarta.persistence.Converter;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "razon_social", length = 150)
    private String razonSocial;

    @Column(name = "rfc", nullable = false, unique = true, length = 13)
    private String rfc;

    @Convert(converter = RegimenFiscalConverter.class)
    @Column(name = "regimen_fiscal", nullable = false, length = 3)
    private RegimenFiscal regimenFiscal;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", columnDefinition = "JSON")
    private String direccion;

    @Column(name = "limite_credito", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal limiteCredito = BigDecimal.ZERO;

    @Column(name = "dias_credito", nullable = false)
    @Builder.Default
    private Integer diasCredito = 0;

    @Column(name = "saldo_pendiente", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal saldoPendiente = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lista_precio_id")
    @ToString.Exclude
    private ListaPrecio listaPrecio;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Builder.Default
    @OneToMany(mappedBy = "cliente", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Venta> ventas = new ArrayList<>();

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