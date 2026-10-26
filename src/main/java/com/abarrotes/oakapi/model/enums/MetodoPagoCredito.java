package com.abarrotes.oakapi.model.enums;

/**
 * Medio de pago con el que se ABONA un adeudo a crédito.
 * Deliberadamente NO incluye CREDITO: un pago de crédito no se paga con crédito.
 * Alineado con el ENUM de la columna pago_credito.metodo_pago (V9).
 */
public enum MetodoPagoCredito {
    EFECTIVO,
    TARJETA,
    TRANSFERENCIA,
    CHEQUE,
    OTRO
}