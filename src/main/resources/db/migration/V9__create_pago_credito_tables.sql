-- V9__create_pago_credito_tables.sql
-- Crear tablas de pagos de crédito y aplicaciones FIFO

CREATE TABLE `pago_credito` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `cliente_id` INT NOT NULL,
    `monto` DECIMAL(12,2) NOT NULL,
    `fecha` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `metodo_pago` ENUM('EFECTIVO','TARJETA','TRANSFERENCIA','CHEQUE','OTRO') NOT NULL DEFAULT 'EFECTIVO',
    `referencia` VARCHAR(100) COMMENT 'Referencia bancaria, número de cheque, etc.',
    `aplicado` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Si ya se aplicó FIFO',
    `monto_restante` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Monto pendiente de aplicar',
    `usuario_id` INT NOT NULL COMMENT 'Usuario que registra el pago',
    `anulado` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Si fue anulado por admin',
    `anulado_por` INT NULL,
    `anulado_en` TIMESTAMP NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_pago_credito_cliente` (`cliente_id`),
    KEY `idx_pago_credito_fecha` (`fecha`),
    KEY `idx_pago_credito_aplicado` (`aplicado`),
    KEY `idx_pago_credito_anulado` (`anulado`),
    CONSTRAINT `fk_pago_credito_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_pago_credito_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_pago_credito_anulado_por` FOREIGN KEY (`anulado_por`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `pago_credito_aplicacion` (
    `pago_id` INT NOT NULL,
    `venta_id` CHAR(36) NOT NULL COMMENT 'UUID de la venta (factura) a la que se aplica',
    `monto_aplicado` DECIMAL(12,2) NOT NULL,
    `fecha_aplicacion` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`pago_id`, `venta_id`),
    KEY `idx_pago_aplicacion_venta` (`venta_id`),
    CONSTRAINT `fk_pago_aplicacion_pago` FOREIGN KEY (`pago_id`) REFERENCES `pago_credito` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_pago_aplicacion_venta` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`uuid`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;