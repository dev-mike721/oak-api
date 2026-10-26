package com.abarrotes.oakapi.model;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImpuestoId implements Serializable {

    private Integer productoId;
    private Integer impuestoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductoImpuestoId that = (ProductoImpuestoId) o;
        return Objects.equals(productoId, that.productoId) && Objects.equals(impuestoId, that.impuestoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productoId, impuestoId);
    }
}