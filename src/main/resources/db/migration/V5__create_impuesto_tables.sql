-- V5__create_impuesto_tables.sql
-- Crear catálogo de impuestos y asignación a productos

CREATE TABLE `impuesto` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `clave` VARCHAR(20) NOT NULL COMMENT 'Clave SAT: IVA16, IVA8, IVA0, IEPS, RET_IVA, RET_ISR',
    `nombre` VARCHAR(100) NOT NULL,
    `tasa_traslado` DECIMAL(5,4) NOT NULL DEFAULT 0.0000 COMMENT 'Tasa de traslado (ej: 0.1600 para IVA 16%)',
    `tasa_retencion` DECIMAL(5,4) NOT NULL DEFAULT 0.0000 COMMENT 'Tasa de retención (ej: 0.106666 para RET IVA)',
    `tipo` ENUM('TRASLADO','RETENCION','AMBOS') NOT NULL DEFAULT 'TRASLADO',
    `activo` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_impuesto_clave` (`clave`),
    KEY `idx_impuesto_activo` (`activo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `producto_impuesto` (
    `producto_id` INT NOT NULL,
    `impuesto_id` INT NOT NULL,
    `tasa_override` DECIMAL(5,4) NULL COMMENT 'Override de tasa para este producto (NULL = usa tasa del catálogo)',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`producto_id`, `impuesto_id`),
    KEY `idx_producto_impuesto_impuesto` (`impuesto_id`),
    CONSTRAINT `fk_producto_impuesto_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_producto_impuesto_impuesto` FOREIGN KEY (`impuesto_id`) REFERENCES `impuesto` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add foreign key constraint for impuesto_id in presupuesto_detalle (created in V4)
ALTER TABLE `presupuesto_detalle`
    ADD CONSTRAINT `fk_presupuesto_detalle_impuesto` FOREIGN KEY (`impuesto_id`) REFERENCES `impuesto` (`id`) ON DELETE SET NULL;