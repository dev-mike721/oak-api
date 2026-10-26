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
public class ClientePrecioEspecialId implements Serializable {

    private Integer clienteId;
    private Integer productoId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientePrecioEspecialId that = (ClientePrecioEspecialId) o;
        return Objects.equals(clienteId, that.clienteId) && Objects.equals(productoId, that.productoId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clienteId, productoId);
    }
}