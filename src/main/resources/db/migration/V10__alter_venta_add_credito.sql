-- V10__alter_venta_add_credito.sql
-- Extender tabla ventas para crédito, presupuesto y caja

ALTER TABLE `ventas`
    ADD COLUMN `saldo_pendiente` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Saldo pendiente de cobro (para crédito)' AFTER `total`,
    ADD COLUMN `fecha_vencimiento` DATE NULL COMMENT 'Fecha límite de pago (para crédito)' AFTER `saldo_pendiente`,
    ADD COLUMN `presupuesto_id` CHAR(36) NULL COMMENT 'FK a presupuesto convertido' AFTER `fecha_vencimiento`,
    ADD COLUMN `caja_id` INT NULL COMMENT 'FK a caja (denormalizado para reportes)' AFTER `presupuesto_id`,
    ADD CONSTRAINT `fk_ventas_presupuesto` FOREIGN KEY (`presupuesto_id`) REFERENCES `presupuesto` (`id`) ON DELETE SET NULL,
    ADD CONSTRAINT `fk_ventas_caja` FOREIGN KEY (`caja_id`) REFERENCES `caja` (`id`) ON DELETE SET NULL;

-- Índices para aging CxC y reportes
CREATE INDEX `idx_ventas_cliente_fecha_venc` ON `ventas` (`cliente_id`, `fecha_vencimiento`);
CREATE INDEX `idx_ventas_caja_fecha` ON `ventas` (`caja_id`, `fecha_hora`);
CREATE INDEX `idx_ventas_presupuesto` ON `ventas` (`presupuesto_id`);