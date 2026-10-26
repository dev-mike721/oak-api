-- V12__seed_impuestos_sat.sql
-- Poblar catálogo base de impuestos (claves SAT México)

INSERT INTO `impuesto` (`clave`, `nombre`, `tasa_traslado`, `tasa_retencion`, `tipo`, `activo`) VALUES
('IVA16', 'IVA 16%', 0.1600, 0.0000, 'TRASLADO', TRUE),
('IVA8', 'IVA 8% (Frontera)', 0.0800, 0.0000, 'TRASLADO', TRUE),
('IVA0', 'IVA 0% (Exento)', 0.0000, 0.0000, 'TRASLADO', TRUE),
('IEPS', 'IEPS (Impuesto Especial)', 0.0800, 0.0000, 'TRASLADO', TRUE),
('RET_IVA', 'Retención IVA 10.6666%', 0.0000, 0.106666, 'RETENCION', TRUE),
('RET_ISR', 'Retención ISR 1.25%', 0.0000, 0.0125, 'RETENCION', TRUE);

-- Verificación
-- SELECT * FROM `impuesto` ORDER BY `id`;