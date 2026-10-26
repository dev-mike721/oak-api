package com.abarrotes.oakapi.converter;

import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Persiste el régimen fiscal como CLAVE SAT ("601", "603"...) y no como el nombre
 * del enum, que es lo que la columna documenta y lo que espera el frontend.
 *
 * La lectura es tolerante: acepta tanto la clave SAT como el nombre completo
 * del enum, de modo que registros guardados antes de este converter siguen
 * siendo legibles.
 */
@Converter(autoApply = false)
public class RegimenFiscalConverter implements AttributeConverter<RegimenFiscal, String> {

    @Override
    public String convertToDatabaseColumn(RegimenFiscal attribute) {
        return attribute == null ? null : attribute.getClaveSat();
    }

    @Override
    public RegimenFiscal convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }

        String value = dbData.trim();

        // 1) Clave SAT
        for (RegimenFiscal regimen : RegimenFiscal.values()) {
            if (regimen.getClaveSat().equals(value)) {
                return regimen;
            }
        }

        // 2) Nombre completo del enum (datos previos a este converter)
        try {
            return RegimenFiscal.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            // 3) Último recurso: prefijo del nombre (p.ej. "601-GENERAL_LEY_...")
            for (RegimenFiscal regimen : RegimenFiscal.values()) {
                if (regimen.name().startsWith(value) || value.startsWith(regimen.getClaveSat())) {
                    return regimen;
                }
            }
            throw new IllegalArgumentException("Régimen fiscal desconocido en la base de datos: '" + dbData + "'");
        }
    }
}