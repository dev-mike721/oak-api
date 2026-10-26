package com.abarrotes.oakapi.model.enums;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RegimenFiscal {
    GENERAL_LEY_PERSONAS_MORALES("601"),
    PERSONAS_MORALES_FINES_NO_LUCRATIVOS("603"),
    SUELDOS_SALARIOS("605"),
    ARRENDAMIENTO("606"),
    REGIMEN_FISCAL_PREFERENTE("608"),
    INCORPORACION_FISCAL("610"),
    INCORPORACION_ACTIVIDADES_EMPRESARIALES("612"),
    INCORPORACION_ACTIVIDADES_PROFESIONALES("614"),
    INCORPORACION_ARRENDAMIENTO("616"),
    SIMPLIFICADO_CONFIA("620"),
    PERSONAS_FISICAS_ACTIVIDADES_EMPRESARIALES_PROFESIONALES("621"),
    PERSONAS_FISICAS_INGRESOS_SUELDOS_SALARIOS("622"),
    PERSONAS_FISICAS_INGRESOS_ARRENDAMIENTO("623"),
    PERSONAS_FISICAS_INGRESOS_DIVIDENDOS("624"),
    PERSONAS_FISICAS_INGRESOS_OTROS("625"),
    SIN_OBLIGACIONES_FISCALES("626");

    private final String claveSat;

    RegimenFiscal(String claveSat) {
        this.claveSat = claveSat;
    }

    public String getClaveSat() {
        return claveSat;
    }

    /**
     * Permite deserializar tanto la clave SAT ("601") como el nombre del enum
     * ("GENERAL_LEY_PERSONAS_MORALES"), para que el frontend pueda usar la
     * clave que documenta el OpenAPI.
     */
    @JsonCreator
    public static RegimenFiscal fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        for (RegimenFiscal regimen : values()) {
            if (regimen.claveSat.equals(trimmed) || regimen.name().equals(trimmed)) {
                return regimen;
            }
        }
        throw new IllegalArgumentException("Régimen fiscal inválido: '" + value + "'");
    }

    @JsonValue
    public String toJsonValue() {
        return claveSat;
    }
}