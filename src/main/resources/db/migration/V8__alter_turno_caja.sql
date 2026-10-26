-- V8__alter_turno_caja.sql
-- Extender turno_caja para multicaja y autorización

ALTER TABLE `turnos_caja`
    ADD COLUMN `caja_id` INT NOT NULL COMMENT 'FK a caja física' AFTER `usuario_id`,
    ADD COLUMN `solicitado_por_id` INT NOT NULL COMMENT 'Usuario que solicita apertura' AFTER `caja_id`,
    ADD COLUMN `autorizado_por_id` INT NULL COMMENT 'Admin/Gerente que autoriza' AFTER `solicitado_por_id`,
    ADD COLUMN `estado_solicitud` ENUM('SOLICITADO','AUTORIZADO','RECHAZADO') NOT NULL DEFAULT 'SOLICITADO' AFTER `estado`,
    ADD COLUMN `motivo_rechazo` VARCHAR(255) NULL AFTER `estado_solicitud`,
    ADD COLUMN `autorizado_en` TIMESTAMP NULL AFTER `motivo_rechazo`,
    ADD CONSTRAINT `fk_turno_caja_caja` FOREIGN KEY (`caja_id`) REFERENCES `caja` (`id`) ON DELETE RESTRICT,
    ADD CONSTRAINT `fk_turno_caja_solicitado_por` FOREIGN KEY (`solicitado_por_id`) REFERENCES `usuarios` (`id`) ON DELETE RESTRICT,
    ADD CONSTRAINT `fk_turno_caja_autorizado_por` FOREIGN KEY (`autorizado_por_id`) REFERENCES `usuarios` (`id`) ON DELETE SET NULL;

-- Índices para consultas frecuentes
CREATE INDEX `idx_turno_caja_caja_estado` ON `turnos_caja` (`caja_id`, `estado`);
CREATE INDEX `idx_turno_caja_solicitud` ON `turnos_caja` (`estado_solicitud`);
CREATE INDEX `idx_turno_caja_usuario_estado` ON `turnos_caja` (`usuario_id`, `estado`);