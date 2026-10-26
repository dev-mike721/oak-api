package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.model.Usuario;

import java.util.Map;

/**
 * Registro de trazabilidad de las operaciones sensibles del sistema.
 *
 * Deliberadamente no propaga excepciones: perder una entrada de auditoria nunca
 * debe tumbar la operacion de negocio que ya se ejecuto.
 */
public interface AuditoriaService {

    /**
     * Registra una accion sensible.
     *
     * @param usuario        autor de la accion (puede ser null si no hay sesion)
     * @param accion         codigo corto, ej. "VENTA_CANCELADA"
     * @param detalles       datos relevantes de la operacion
     */
    void registrar(Usuario usuario, String accion, Map<String, Object> detalles);

    void registrar(String accion, Map<String, Object> detalles);

    void registrar(String accion, String detalles);
}