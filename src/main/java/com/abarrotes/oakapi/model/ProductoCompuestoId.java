package com.abarrotes.oakapi.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ProductoCompuestoId implements Serializable {

    @Column(name = "producto_padre_id")
    private Integer productoPadreId;

    @Column(name = "producto_hijo_id")
    private Integer productoHijoId;
}
