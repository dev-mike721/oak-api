-- V1__initial_schema.sql
-- Initial schema for oak-api - tables ordered by dependency

-- 1. Categorias (no dependencies)
DROP TABLE IF EXISTS `categorias`;
CREATE TABLE `categorias` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(50) NOT NULL,
  `descripcion` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `nombre` (`nombre`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Proveedores (no dependencies)
DROP TABLE IF EXISTS `proveedores`;
CREATE TABLE `proveedores` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `razon_social` varchar(120) NOT NULL,
  `rfc` varchar(20) DEFAULT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `direccion` text DEFAULT NULL,
  `dias_credito` int(11) NOT NULL DEFAULT 0,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_registro` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Usuarios (no dependencies)
DROP TABLE IF EXISTS `usuarios`;
CREATE TABLE `usuarios` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `rol` enum('ADMINISTRADOR','CAJERO','SUPERVISOR') NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Clientes (no dependencies)
DROP TABLE IF EXISTS `cliente`;
CREATE TABLE `cliente` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `nombre` VARCHAR(100) NOT NULL,
    `razon_social` VARCHAR(150),
    `rfc` VARCHAR(13) NOT NULL,
    `regimen_fiscal` VARCHAR(100) NOT NULL COMMENT 'Clave SAT régimen fiscal (ej: 601, 603, 605, 606, 608, 610, 612, 614, 616, 620, 621, 622, 623, 624, 625, 626)',
    `email` VARCHAR(100),
    `telefono` VARCHAR(20),
    `direccion` JSON COMMENT 'Dirección completa: calle, numero, colonia, ciudad, estado, cp',
    `limite_credito` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Límite máximo de crédito permitido',
    `dias_credito` INT NOT NULL DEFAULT 0 COMMENT 'Días de crédito concedidos',
    `saldo_pendiente` DECIMAL(12,2) NOT NULL DEFAULT 0.00 COMMENT 'Saldo actual pendiente de pago',
    `lista_precio_id` INT NULL COMMENT 'FK a lista_precio asignada al cliente',
    `activo` BOOLEAN NOT NULL DEFAULT TRUE,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_cliente_rfc` (`rfc`),
    KEY `idx_cliente_lista_precio` (`lista_precio_id`),
    KEY `idx_cliente_activo` (`activo`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Productos (depends on categorias)
DROP TABLE IF EXISTS `productos`;
CREATE TABLE `productos` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `plu` varchar(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `descripcion` text DEFAULT NULL,
  `categoria_id` int(11) NOT NULL,
  `unidad_medida` enum('PIEZA','KG','GRAMO','LITRO') NOT NULL,
  `permite_decimales` tinyint(1) NOT NULL DEFAULT 0,
  `precio_base` decimal(10,2) NOT NULL,
  `es_compuesto` tinyint(1) NOT NULL DEFAULT 0,
  `activo` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `plu` (`plu`),
  KEY `fk_productos_categoria` (`categoria_id`),
  KEY `idx_producto_plu` (`plu`),
  CONSTRAINT `fk_productos_categoria` FOREIGN KEY (`categoria_id`) REFERENCES `categorias` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Cliente_Precio_Especial (depends on cliente, productos)
DROP TABLE IF EXISTS `cliente_precio_especial`;
CREATE TABLE `cliente_precio_especial` (
    `cliente_id` INT NOT NULL,
    `producto_id` INT NOT NULL,
    `precio` DECIMAL(10,2) NOT NULL,
    `vigencia_inicio` TIMESTAMP NOT NULL,
    `vigencia_fin` TIMESTAMP NULL COMMENT 'NULL = sin fecha fin',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`cliente_id`, `producto_id`),
    KEY `idx_cliente_precio_vigencia` (`vigencia_inicio`, `vigencia_fin`),
    CONSTRAINT `fk_cliente_precio_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cliente_precio_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Productos_Compuestos (depends on productos)
DROP TABLE IF EXISTS `productos_compuestos`;
CREATE TABLE `productos_compuestos` (
  `producto_padre_id` int(11) NOT NULL,
  `producto_hijo_id` int(11) NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  PRIMARY KEY (`producto_padre_id`,`producto_hijo_id`),
  KEY `fk_compuestos_hijo` (`producto_hijo_id`),
  CONSTRAINT `fk_compuestos_hijo` FOREIGN KEY (`producto_hijo_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `fk_compuestos_padre` FOREIGN KEY (`producto_padre_id`) REFERENCES `productos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Promociones (no dependencies)
DROP TABLE IF EXISTS `promociones`;
CREATE TABLE `promociones` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` varchar(100) NOT NULL,
  `tipo` enum('DESCUENTO_PORCENTAJE','TRES_POR_DOS','PRECIO_COMBO') NOT NULL,
  `valor_descuento` decimal(10,2) DEFAULT NULL,
  `fecha_inicio` datetime NOT NULL,
  `fecha_fin` datetime NOT NULL,
  `dias_semana` varchar(20) NOT NULL DEFAULT '1,2,3,4,5,6,7',
  `activa` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Promocion_Productos (depends on promociones, productos)
DROP TABLE IF EXISTS `promocion_productos`;
CREATE TABLE `promocion_productos` (
  `promocion_id` int(11) NOT NULL,
  `producto_id` int(11) NOT NULL,
  PRIMARY KEY (`promocion_id`,`producto_id`),
  KEY `fk_promocion_producto` (`producto_id`),
  CONSTRAINT `fk_promocion_fk` FOREIGN KEY (`promocion_id`) REFERENCES `promociones` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_promocion_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Precios_Volumen (depends on productos)
DROP TABLE IF EXISTS `precios_volumen`;
CREATE TABLE `precios_volumen` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producto_id` int(11) NOT NULL,
  `cantidad_minima` decimal(10,3) NOT NULL,
  `precio_unitario` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_volumen_producto` (`producto_id`),
  CONSTRAINT `fk_volumen_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Compras (depends on proveedores, usuarios)
DROP TABLE IF EXISTS `compras`;
CREATE TABLE `compras` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `proveedor_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `folio_factura` varchar(50) DEFAULT NULL,
  `fecha_compra` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_vencimiento_pago` date DEFAULT NULL,
  `total` decimal(10,2) NOT NULL,
  `estado` enum('PENDIENTE_PAGO','PAGADA','CANCELADA') NOT NULL DEFAULT 'PENDIENTE_PAGO',
  PRIMARY KEY (`id`),
  KEY `fk_compra_proveedor` (`proveedor_id`),
  KEY `fk_compra_usuario` (`usuario_id`),
  CONSTRAINT `fk_compra_proveedor` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`),
  CONSTRAINT `fk_compra_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Lotes (depends on productos)
DROP TABLE IF EXISTS `lotes`;
CREATE TABLE `lotes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `producto_id` int(11) NOT NULL,
  `codigo_lote` varchar(50) NOT NULL,
  `costo_compra` decimal(10,2) NOT NULL,
  `cantidad_inicial` decimal(10,3) NOT NULL,
  `cantidad_actual` decimal(10,3) NOT NULL,
  `fecha_recepcion` date NOT NULL,
  `fecha_caducidad` date DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_lote_producto_codigo` (`producto_id`,`codigo_lote`),
  KEY `idx_lote_fifo` (`producto_id`,`fecha_caducidad`,`cantidad_actual`),
  CONSTRAINT `fk_lotes_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Detalle_Compras (depends on compras, productos, lotes)
DROP TABLE IF EXISTS `detalle_compras`;
CREATE TABLE `detalle_compras` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `compra_id` int(11) NOT NULL,
  `producto_id` int(11) NOT NULL,
  `lote_id` int(11) NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  `costo_unitario` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_detcompra_compra` (`compra_id`),
  KEY `fk_detcompra_producto` (`producto_id`),
  KEY `fk_detcompra_lote` (`lote_id`),
  CONSTRAINT `fk_detcompra_compra` FOREIGN KEY (`compra_id`) REFERENCES `compras` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_detcompra_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`),
  CONSTRAINT `fk_detcompra_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Movimientos_Inventario (depends on lotes, usuarios)
DROP TABLE IF EXISTS `movimientos_inventario`;
CREATE TABLE `movimientos_inventario` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lote_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `tipo_movimiento` enum('ENTRADA_COMPRA','SALIDA_VENTA','AJUSTE_POSITIVO','AJUSTE_NEGATIVO','MERMA') NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  `cantidad_anterior` decimal(10,3) NOT NULL,
  `cantidad_nueva` decimal(10,3) NOT NULL,
  `motivo` varchar(150) DEFAULT NULL,
  `documento_tipo` enum('COMPRA','VENTA','MANUAL') DEFAULT NULL,
  `documento_id` varchar(36) DEFAULT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_mov_usuario` (`usuario_id`),
  KEY `idx_mov_lote` (`lote_id`),
  KEY `idx_mov_fecha` (`fecha_hora`),
  CONSTRAINT `fk_mov_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`),
  CONSTRAINT `fk_mov_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Turnos_Caja (depends on usuarios)
DROP TABLE IF EXISTS `turnos_caja`;
CREATE TABLE `turnos_caja` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `fecha_apertura` datetime NOT NULL DEFAULT current_timestamp(),
  `monto_inicial` decimal(10,2) NOT NULL,
  `fecha_cierre` datetime DEFAULT NULL,
  `monto_fisico_conteo` decimal(10,2) DEFAULT NULL,
  `monto_sistema_calculado` decimal(10,2) DEFAULT NULL,
  `diferencia` decimal(10,2) DEFAULT NULL,
  `estado` enum('ABIERTO','CERRADO') NOT NULL DEFAULT 'ABIERTO',
  PRIMARY KEY (`id`),
  KEY `fk_turno_usuario` (`usuario_id`),
  CONSTRAINT `fk_turno_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Ventas (depends on turnos_caja, usuarios, cliente)
DROP TABLE IF EXISTS `ventas`;
CREATE TABLE `ventas` (
  `uuid` char(36) NOT NULL,
  `turno_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `cliente_id` int(11) DEFAULT NULL,
  `fecha_hora` datetime NOT NULL,
  `metodo_pago` enum('EFECTIVO','TARJETA','CREDITO') NOT NULL,
  `total` decimal(10,2) NOT NULL,
  `estado` enum('COMPLETADA','CANCELADA') NOT NULL DEFAULT 'COMPLETADA',
  `sincronizado` tinyint(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`uuid`),
  KEY `fk_ventas_turno` (`turno_id`),
  KEY `fk_ventas_usuario` (`usuario_id`),
  KEY `fk_ventas_cliente` (`cliente_id`),
  KEY `idx_ventas_fecha` (`fecha_hora`),
  CONSTRAINT `fk_ventas_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  CONSTRAINT `fk_ventas_turno` FOREIGN KEY (`turno_id`) REFERENCES `turnos_caja` (`id`),
  CONSTRAINT `fk_ventas_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Detalle_Ventas (depends on ventas, productos, lotes)
DROP TABLE IF EXISTS `detalle_ventas`;
CREATE TABLE `detalle_ventas` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `venta_uuid` char(36) NOT NULL,
  `producto_id` int(11) NOT NULL,
  `lote_id` int(11) NOT NULL,
  `cantidad` decimal(10,3) NOT NULL,
  `precio_unitario_aplicado` decimal(10,2) NOT NULL,
  `subtotal` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_detalle_venta` (`venta_uuid`),
  KEY `fk_detalle_producto` (`producto_id`),
  KEY `fk_detalle_lote` (`lote_id`),
  CONSTRAINT `fk_detalle_lote` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`),
  CONSTRAINT `fk_detalle_producto` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  CONSTRAINT `fk_detalle_venta` FOREIGN KEY (`venta_uuid`) REFERENCES `ventas` (`uuid`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Abonos_Credito (depends on cliente, usuarios)
DROP TABLE IF EXISTS `abonos_credito`;
CREATE TABLE `abonos_credito` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `cliente_id` int(11) NOT NULL,
  `usuario_id` int(11) NOT NULL,
  `monto` decimal(10,2) NOT NULL,
  `saldo_anterior` decimal(10,2) NOT NULL,
  `saldo_nuevo` decimal(10,2) NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_abono_cliente` (`cliente_id`),
  KEY `fk_abono_usuario` (`usuario_id`),
  CONSTRAINT `fk_abono_cliente` FOREIGN KEY (`cliente_id`) REFERENCES `cliente` (`id`),
  CONSTRAINT `fk_abono_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Logs_Auditoria (depends on usuarios)
DROP TABLE IF EXISTS `logs_auditoria`;
CREATE TABLE `logs_auditoria` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `usuario_id` int(11) NOT NULL,
  `autorizado_por_id` int(11) DEFAULT NULL,
  `accion` varchar(100) NOT NULL,
  `detalles` text NOT NULL,
  `fecha_hora` datetime NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_log_usuario` (`usuario_id`),
  KEY `fk_log_autorizado` (`autorizado_por_id`),
  CONSTRAINT `fk_log_autorizado` FOREIGN KEY (`autorizado_por_id`) REFERENCES `usuarios` (`id`),
  CONSTRAINT `fk_log_usuario` FOREIGN KEY (`usuario_id`) REFERENCES `usuarios` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Detalle_Ventas_Impuesto (depends on detalle_ventas, impuesto - created in later migration)
-- This table is created in V11__create_detalle_venta_impuesto.sql