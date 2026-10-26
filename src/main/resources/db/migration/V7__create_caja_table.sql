-- V7__create_caja_table.sql
-- Crear tabla de cajas físicas para multicaja

CREATE TABLE `caja` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(50) NOT NULL COMMENT 'Ej: Caja 1 - Entrada, Caja 2 - Fondo',
    `ubicacion` VARCHAR(100) COMMENT 'Ubicación física en el local',
    `activa` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_caja_nombre` (`nombre`),
    KEY `idx_caja_activa` (`activa`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos semilla opcionales
INSERT INTO `caja` (`nombre`, `ubicacion`, `activa`) VALUES
('Caja 1 - Entrada', 'Entrada principal', TRUE),
('Caja 2 - Fondo', 'Fondo del local', TRUE);