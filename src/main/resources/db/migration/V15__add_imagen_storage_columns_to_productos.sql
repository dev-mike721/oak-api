-- V15__add_imagen_storage_columns_to_productos.sql
-- Agregar columnas para metadatos de almacenamiento de imágenes locales

ALTER TABLE productos 
    ADD COLUMN imagen_storage_key VARCHAR(500) NULL COMMENT 'Ruta relativa del archivo en storage local (plu/timestamp-uuid.ext)',
    ADD COLUMN imagen_content_type VARCHAR(100) NULL COMMENT 'Content-Type real del archivo detectado por Tika',
    ADD COLUMN imagen_nombre_original VARCHAR(255) NULL COMMENT 'Nombre original del archivo subido por el usuario',
    ADD COLUMN imagen_size_bytes BIGINT NULL COMMENT 'Tamaño del archivo en bytes',
    ADD COLUMN imagen_updated_at TIMESTAMP NULL COMMENT 'Fecha de última actualización de la imagen';