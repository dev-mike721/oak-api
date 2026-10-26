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
public class PagoCreditoAplicacionId implements Serializable {

    private Integer pagoId;
    private String ventaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PagoCreditoAplicacionId that = (PagoCreditoAplicacionId) o;
        return Objects.equals(pagoId, that.pagoId) && Objects.equals(ventaId, that.ventaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pagoId, ventaId);
    }
}