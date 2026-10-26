package com.abarrotes.oakapi.model.enums;

/**
 * Roles del sistema, alineados con el ENUM de la columna usuarios.rol (V13).
 *
 * SUPERVISOR se elimino: no tenia permisos asignados en SecurityConfig ni filas
 * en la base, por lo que un usuario con ese rol podia autenticarse pero fallaba
 * en todos los endpoints.
 */
public enum Rol {
    ADMINISTRADOR,
    GERENTE,
    CAJERO
}