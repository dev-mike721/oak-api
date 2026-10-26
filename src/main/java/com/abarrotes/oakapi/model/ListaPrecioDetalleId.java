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
public class ListaPrecioDetalleId implements Serializable {

    private Integer listaPrecioId;
    private Integer productoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListaPrecioDetalleId that = (ListaPrecioDetalleId) o;
        return Objects.equals(listaPrecioId, that.listaPrecioId) && Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(listaPrecioId, productoId);
    }
}