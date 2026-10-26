-- V6__create_serie_table.sql
-- Crear tabla de series/IMEI para trazabilidad unitaria

CREATE TABLE `serie` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `producto_id` INT NOT NULL,
    `lote_id` INT NULL COMMENT 'FK al lote (nullable para migración gradual)',
    `codigo_serie` VARCHAR(100) NOT NULL COMMENT 'IMEI, número de serie, código único',
    `estado` ENUM('DISPONIBLE','VENDIDO','RESERVADO','GARANTIA','DEVUELTO') NOT NULL DEFAULT 'DISPONIBLE',
    `venta_id` CHAR(36) NULL COMMENT 'UUID de la venta donde se vendió',
    `fecha_venta` TIMESTAMP NULL COMMENT 'Fecha de venta',
    `fecha_garantia` TIMESTAMP NULL COMMENT 'Fecha de entrada a garantía',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_serie_codigo` (`codigo_serie`),
    KEY `idx_serie_producto_estado` (`producto_id`, `estado`),
    KEY `idx_serie_lote` (`lote_id`),
    KEY `idx_serie_venta` (`venta_id`),
    CONSTRAINT `fk_serie_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_serie_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`) ON DELETE SET NULL,
    CONSTRAINT `fk_serie_venta` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`uuid`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;