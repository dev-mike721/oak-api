package com.abarrotes.oakapi.repository;

public interface ValorizacionCategoriaProjection {
    String getCategoria();

    java.math.BigDecimal getValor();

    Long getLotes();
}
