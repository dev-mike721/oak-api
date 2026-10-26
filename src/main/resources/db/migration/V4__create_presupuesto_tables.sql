-- V4__create_presupuesto_tables.sql
-- Crear tablas de presupuestos/cotizaciones

CREATE TABLE `presupuesto` (
    `id` CHAR(36) NOT NULL COMMENT 'UUID',
    `cliente_id` INT NOT NULL,
    `usuario_id` INT NOT NULL COMMENT 'Vendedor que crea el presupuesto',
    `fecha` DATE NOT NULL,
    `vigencia_dias` INT NOT NULL DEFAULT 30,
    `estado` ENUM('BORRADOR','ENVIADO','APROBADO','VENCIDO','CONVERTIDO','CANCELADO') NOT NULL DEFAULT 'BORRADOR',
    `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `impuestos_total` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `total` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `observaciones` TEXT,
    `aprobado_por` INT NULL COMMENT 'Usuario que aprueba (cliente o gerente)',
    `aprobado_en` TIMESTAMP NULL,
    `venta_uuid` CHAR(36) NULL COMMENT 'UUID de la venta generada al convertir',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_presupuesto_cliente` (`cliente_id`),
    KEY `idx_presupuesto_usuario` (`usuario_id`),
    KEY `idx_presupuesto_estado` (`estado`),
    KEY `idx_presupuesto_fecha` (`fecha`),
    KEY `idx_presupuesto_vigencia` (`fecha`, `vigencia_dias`),
    CONSTRAINT `fk_presupuesto_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_presupuesto_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_presupuesto_aprobado_por` FOREIGN KEY (`aprobado_por`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `presupuesto_detalle` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `presupuesto_id` CHAR(36) NOT NULL,
    `producto_id` INT NOT NULL,
    `cantidad` DECIMAL(10,3) NOT NULL,
    `precio_unitario` DECIMAL(10,2) NOT NULL,
    `impuesto_id` INT NULL COMMENT 'Impuesto principal aplicado',
    `impuesto_tasa` DECIMAL(5,4) NOT NULL DEFAULT 0.0000 COMMENT 'Tasa efectiva usada (para snapshot)',
    `impuesto_importe` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `subtotal` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `total` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_presupuesto_detalle_presupuesto` (`presupuesto_id`),
    KEY `idx_presupuesto_detalle_producto` (`producto_id`),
    CONSTRAINT `fk_presupuesto_detalle_presupuesto` FOREIGN KEY (`presupuesto_id`) REFERENCES `presupuesto` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_presupuesto_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;