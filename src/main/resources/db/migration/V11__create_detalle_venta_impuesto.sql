-- V11__create_detalle_venta_impuesto.sql
-- Crear tabla de desglose de impuestos por línea de venta

CREATE TABLE `detalle_venta_impuesto` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `detalle_venta_id` BIGINT NOT NULL COMMENT 'FK a detalle_ventas',
    `impuesto_id` INT NOT NULL COMMENT 'FK a impuesto',
    `base` DECIMAL(12,2) NOT NULL COMMENT 'Base imponible',
    `tasa` DECIMAL(5,4) NOT NULL COMMENT 'Tasa aplicada (snapshot)',
    `importe_traslado` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Importe de traslado (IVA, IEPS)',
    `importe_retencion` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Importe de retención (RET_IVA, RET_ISR)',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_dvi_detalle_venta` (`detalle_venta_id`),
    KEY `idx_dvi_impuesto` (`impuesto_id`),
    CONSTRAINT `fk_dvi_detalle_venta` FOREIGN KEY (`detalle_venta_id`) REFERENCES `detalle_ventas` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_dvi_impuesto` FOREIGN KEY (`impuesto_id`) REFERENCES `impuesto` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;