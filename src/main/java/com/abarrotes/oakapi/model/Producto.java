package com.abarrotes.oakapi.model;

import com.abarrotes.oakapi.model.enums.UnidadMedida;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "plu", nullable = false, unique = true, length = 20)
    private String plu;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_medida", nullable = false)
    private UnidadMedida unidadMedida;

    @Builder.Default
    @Column(name = "permite_decimales", nullable = false)
    private Boolean permiteDecimales = false;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Builder.Default
    @Column(name = "es_compuesto", nullable = false)
    private Boolean esCompuesto = false;

    @Builder.Default
    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl;

    @Column(name = "imagen_storage_key", length = 500)
    private String imagenStorageKey;

    @Column(name = "imagen_content_type", length = 100)
    private String imagenContentType;

    @Column(name = "imagen_nombre_original", length = 255)
    private String imagenNombreOriginal;

    @Column(name = "imagen_size_bytes")
    private Long imagenSizeBytes;

    @Column(name = "imagen_updated_at")
    private LocalDateTime imagenUpdatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "productoPadre")
    @ToString.Exclude
    private List<ProductoCompuesto> componentes = new ArrayList<>();
}