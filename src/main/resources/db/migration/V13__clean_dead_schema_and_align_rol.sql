-- V13__clean_dead_schema_and_align_rol.sql
--
-- 1) usuarios.rol: la columna era ENUM('ADMINISTRADOR','CAJERO','SUPERVISOR') pero
--    SecurityConfig autoriza endpoints al rol GERENTE, que no existia en la BD.
--    Crear un usuario GERENTE fallaba con el FK/truncado de datos.
--    SUPERVISOR se elimina: no tiene ni una referencia en el codigo, ninguna fila
--    y ningun permiso en SecurityConfig, asi que un usuario SUPERVISOR podria
--    autenticarse pero fallaria en todos los endpoints.
--
-- 2) Tablas huerfanas: sin entidad, sin repositorio, sin codigo que las use y
--    con 0 filas. El dominio equivalente ya existe en otro lado:
--      - precios_volumen -> lista_precio_detalle (V3)
--      - abonos_credito  -> pago_credito con FIFO (V9)
--      - promociones    -> sin reemplazo previsto
--    Se elimina tambien promocion_productos, que solo existia por su FK a promociones.
--
-- logs_auditoria NO se elimina: es una capacidad diseñada para el POS y borrarla
-- es una decision de gobierno, no de limpieza.

-- 1) Alineacion del rol
ALTER TABLE `usuarios`
  MODIFY COLUMN `rol` ENUM('ADMINISTRADOR','GERENTE','CAJERO') NOT NULL;

-- 2) Limpieza de tablas huerfanas
DROP TABLE IF EXISTS `promocion_productos`;
DROP TABLE IF EXISTS `promociones`;
DROP TABLE IF EXISTS `precios_volumen`;
DROP TABLE IF EXISTS `abonos_credito`;