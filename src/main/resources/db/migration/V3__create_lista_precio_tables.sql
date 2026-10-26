-- V3__create_lista_precio_tables.sql
-- Crear tablas de listas de precios con escalones de volumen

CREATE TABLE `lista_precio` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(100) NOT NULL,
    `descripcion` VARCHAR(255),
    `vigencia_inicio` DATE NOT NULL,
    `vigencia_fin` DATE NULL COMMENT 'NULL = sin fecha fin',
    `activa` BOOLEAN NOT NULL DEFAULT TRUE,
    `es_default_canal` BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'Indica si es la lista de precios por defecto del canal',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_lista_precio_nombre_activa` (`nombre`, `activa`),
    KEY `idx_lista_precio_vigencia` (`vigencia_inicio`, `vigencia_fin`),
    KEY `idx_lista_precio_activa` (`activa`),
    KEY `idx_lista_precio_default` (`es_default_canal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `lista_precio_detalle` (
    `lista_precio_id` INT NOT NULL,
    `producto_id` INT NOT NULL,
    `precio_base` DECIMAL(10,2) NOT NULL,
    `vol_min_1` INT NULL COMMENT 'Volumen mínimo para precio_1',
    `precio_1` DECIMAL(10,2) NULL COMMENT 'Precio para vol_min_1',
    `vol_min_2` INT NULL COMMENT 'Volumen mínimo para precio_2',
    `precio_2` DECIMAL(10,2) NULL COMMENT 'Precio para vol_min_2',
    `vol_min_3` INT NULL COMMENT 'Volumen mínimo para precio_3',
    `precio_3` DECIMAL(10,2) NULL COMMENT 'Precio para vol_min_3',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`lista_precio_id`, `producto_id`),
    KEY `idx_lista_precio_detalle_producto` (`producto_id`),
    CONSTRAINT `fk_lista_precio_detalle_lista` FOREIGN KEY (`lista_precio_id`) REFERENCES `lista_precio` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_lista_precio_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;