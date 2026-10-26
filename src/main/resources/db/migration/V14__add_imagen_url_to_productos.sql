-- V14__add_imagen_url_to_productos.sql
-- Agregar columna imagen_url a la tabla productos para almacenar URLs de imágenes

ALTER TABLE productos ADD COLUMN imagen_url VARCHAR(500) NULL COMMENT 'URL de la imagen del producto';