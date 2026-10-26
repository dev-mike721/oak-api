package com.abarrotes.oakapi;

import com.abarrotes.oakapi.converter.RegimenFiscalConverter;
import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regresión T2: Cliente.regimen_fiscal se documenta como clave SAT ("601") pero
 * se mapeaba con @Enumerated(STRING), que guarda el nombre del enum (hasta 56
 * chars). Cualquier dato que no fuera un nombre de enum reventaba al leer con
 * IllegalArgumentException -> 500.
 *
 * Verificado en produccion: una fila con '601' hacia GET /clientes -> 500.
 */
class RegimenFiscalConverterTest {

    private final RegimenFiscalConverter converter = new RegimenFiscalConverter();

    @Test
    void guardaLaClaveSatYNoElNombreDelEnum() {
        String guardado = converter.convertToDatabaseColumn(RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES);

        assertEquals("601", guardado, "Debe persistir la clave SAT, no el nombre del enum");
        assertTrue(guardado.length() <= 3, "La columna es VARCHAR(3) según la entidad");
    }

    @Test
    void todosLosRegimenesCabenEnTresCaracteres() {
        for (RegimenFiscal regimen : RegimenFiscal.values()) {
            assertEquals(3, regimen.getClaveSat().length(),
                    "La clave SAT de " + regimen.name() + " debe medir 3 caracteres");
        }
    }

    @Test
    void leeClaveSatGuardada() {
        assertEquals(RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES,
                converter.convertToEntityAttribute("601"));
        assertEquals(RegimenFiscal.SIMPLIFICADO_CONFIA,
                converter.convertToEntityAttribute("620"));
    }

    @Test
    void leeNombreCompletoDeDatosPrevios() {
        // Compatibilidad: registros guardados antes del converter
        assertEquals(RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES,
                converter.convertToEntityAttribute("GENERAL_LEY_PERSONAS_MORALES"));
        assertEquals(RegimenFiscal.PERSONAS_FISICAS_ACTIVIDADES_EMPRESARIALES_PROFESIONALES,
                converter.convertToEntityAttribute(
                        "PERSONAS_FISICAS_ACTIVIDADES_EMPRESARIALES_PROFESIONALES"));
    }

    @Test
    void idaYVueltaPreservaTodosLosRegimenes() {
        for (RegimenFiscal regimen : RegimenFiscal.values()) {
            String enBd = converter.convertToDatabaseColumn(regimen);
            assertEquals(regimen, converter.convertToEntityAttribute(enBd));
        }
    }

    @Test
    void nullEsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertNull(converter.convertToEntityAttribute(null));
        assertNull(converter.convertToEntityAttribute("  "));
    }

    @Test
    void datoDesconocidoLanzaErrorDescriptivo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> converter.convertToEntityAttribute("999"));

        assertTrue(ex.getMessage().contains("999"));
    }

    @Test
    void elEnumAceptaClaveSatYNombreDesdeJson() {
        // El frontend puede enviar "601" o el nombre; ambos deben funcionar
        assertEquals(RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES, RegimenFiscal.fromString("601"));
        assertEquals(RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES,
                RegimenFiscal.fromString("GENERAL_LEY_PERSONAS_MORALES"));
        assertNull(RegimenFiscal.fromString(null));
        assertThrows(IllegalArgumentException.class, () -> RegimenFiscal.fromString("999"));
    }

    @Test
    void elEnumSeSerializaComoClaveSat() {
        assertEquals("601", RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES.toJsonValue());
    }

}