-- =====================================================================
-- seed-dev.sql -- Datos de demostracion para una abarrotes
-- =====================================================================
-- Uso:  mysql -uroot -p<pass> tienda_db < src/main/resources/db/seed/seed-dev.sql
--
-- IMPORTANTE: este archivo NO esta en db/migration, asi que Flyway NUNCA lo
-- ejecuta. Es manual y unicamente para desarrollo/pruebas.
--
-- El script RESETEA los datos de demostracion y vuelve a sembrarlos, asi que
-- es re-ejecutable. Conserva el usuario 'admin'.
--
-- Escenario: "Abarrotes La Esquina", tienda de barrio con 2 cajas,
-- 4 empleados (2 cajeros, 1 gerente, 1 admin).
-- =====================================================================

-- ---------------------------------------------------------------------
-- 0. Reset de los datos demo
-- ---------------------------------------------------------------------
SET FOREIGN_KEY_CHECKS = 0;
DELETE FROM movimientos_inventario;
DELETE FROM detalle_ventas;
DELETE FROM pago_credito_aplicacion;
DELETE FROM pago_credito;
DELETE FROM ventas;
DELETE FROM turnos_caja;
DELETE FROM logs_auditoria;
DELETE FROM lotes;
DELETE FROM serie;
DELETE FROM producto_impuesto;
DELETE FROM presupuesto_detalle;
DELETE FROM presupuesto;
DELETE FROM productos;
DELETE FROM cliente_precio_especial;
DELETE FROM cliente;
DELETE FROM proveedores;
DELETE FROM categorias;
DELETE FROM caja;
DELETE FROM usuarios WHERE username <> 'admin';
SET FOREIGN_KEY_CHECKS = 1;

-- ---------------------------------------------------------------------
-- 1. Categorias (5 categorias principales de una tiendita de esquina)
-- ---------------------------------------------------------------------
INSERT INTO categorias (nombre, descripcion) VALUES
  ('Botanas',           'Papas, chicharrones, totopos, nueces'),
  ('Refrescos',         'Refrescos de cola, naranja, lima-limon, agua mineral'),
  ('Cervezas',          'Cervezas nacionales e importadas'),
  ('Enlatados',         'Frijoles, verduras, atun, sardinas enlatados'),
  ('Lacteos',           'Leche, quesos, yogures, crema, huevos');

-- ---------------------------------------------------------------------
-- 2. Proveedores
-- ---------------------------------------------------------------------
INSERT INTO proveedores (razon_social, rfc, telefono, email, direccion, dias_credito, activo) VALUES
  ('PepsiCo Alimentos Mexico S.A. de C.V.',     'PAM010101ABC', '555 1234 5678', 'ventas@pepsico.mx',      'Av. Insurgentes Sur 1234, Col. Insurgentes, CDMX', 30, 1),
  ('Coca-Cola FEMSA S.A. de C.V.',              'CCF010101XYZ', '555 2345 6789', 'pedidos@cocafemsa.mx', 'Av. Universidad 456, Col. del Valle, CDMX', 30, 1),
  ('Grupo Modelo S.A. de C.V.',                 'GMO010101GMO', '555 3456 7890', 'ventas@grupomodelo.mx',  'Av. Insurgentes Norte 789, Col. Buenavista, CDMX', 45, 1),
  ('La Costena S.A. de C.V.',                   'LCO010101LCO', '555 4567 8901', 'ventas@lacostena.mx',    'Carretera Mexico-Puebla Km 15, Puebla', 30, 1),
  ('Lala S.A.B. de C.V.',                       'LAL010101LAL', '555 6789 0123', 'pedidos@lala.com.mx',    'Av. Lazaro Cardenas 333, Torreon, Coah', 0, 1);

-- ---------------------------------------------------------------------
-- 3. Usuarios (el admin ya existe; se crean los operativos)
--    Password de todos: abarrotes123
-- ---------------------------------------------------------------------
INSERT INTO usuarios (nombre, username, password_hash, rol, activo) VALUES
  ('Lucia Fernandez',  'lucia',     '$2a$10$f7IclYm26kDcdoToMf0qJeQZwA4wO/7aymViKAScVSaSlyjjep5uS', 'CAJERO',      1),
  ('Marco Duarte',     'marco',     '$2a$10$f7IclYm26kDcdoToMf0qJeQZwA4wO/7aymViKAScVSaSlyjjep5uS', 'CAJERO',      1),
  ('Rosa Hernandez',   'rosa',      '$2a$10$f7IclYm26kDcdoToMf0qJeQZwA4wO/7aymViKAScVSaSlyjjep5uS', 'ADMINISTRADOR', 1),
  ('Jorge Ramirez',    'jorge',     '$2a$10$f7IclYm26kDcdoToMf0qJeQZwA4wO/7aymViKAScVSaSlyjjep5uS', 'GERENTE',     1);

-- ---------------------------------------------------------------------
-- 4. Clientes (5 clientes variados)
-- ---------------------------------------------------------------------
INSERT INTO cliente (nombre, razon_social, rfc, regimen_fiscal, email, telefono,
                    limite_credito, dias_credito, saldo_pendiente, activo) VALUES
  ('Maria Guadalupe Sanchez Ruiz',      'Sanchez Distribucion',       'SCD0804156R21', '601', 'compras@sanchezdist.mx',   '554 8871 2233', 15000.00, 30,  0.00, 1),
  ('Carlos Eduardo Mendoza Solis',      NULL,                          'CEM9011274H85', '612', 'ce.mendoza@correo.mx',     '552 3391 8844',  8000.00, 15,  0.00, 1),
  ('Panaderia La Espiga',               'Panaderia La Espiga S.A. de C.V.', 'PES150318KJ3', '601', 'pedidos@laespiga.mx',    '556 1122 3344', 25000.00, 45,  0.00, 1),
  ('Jose Luis Ramirez Pena',            NULL,                          'JRP800924RB2',  '605', 'jlramirez@mail.mx',        '553 7744 1122',  5000.00, 15,  0.00, 1),
  ('Ana Sofia Contreras Leon',          NULL,                          'ACL950316MF4',  '620', 'anasofia.contreras@mail.mx','558 2211 9933',  6000.00, 10,  0.00, 1);

-- ---------------------------------------------------------------------
-- 5. Cajas
-- ---------------------------------------------------------------------
INSERT INTO caja (nombre, ubicacion, activa) VALUES
  ('Caja 1 - Mostrador',   'Entrada principal', 1),
  ('Caja 2 - Area de bebidas', 'Junto a la nevera de cervezas', 1);

COMMIT;

-- =============================================================================
-- PRODUCTOS - 5 CATEGORIAS x 2 PRODUCTOS = 10 PRODUCTOS TOTALES
-- =============================================================================

-- 1. BOTANAS (2 productos)
INSERT INTO productos (plu, nombre, descripcion, categoria_id, unidad_medida, permite_decimales, precio_base, es_compuesto, activo, imagen_url)
SELECT s.plu, s.nombre, s.descripcion, c.id, s.um, s.permite_dec, s.precio, 0, 1, s.img
FROM (
  SELECT 'SAB001' AS plu,'Sabritas Clasicas 170g' AS nombre,'Papas fritas clasicas Sabritas 170g' AS descripcion,(SELECT id FROM categorias WHERE nombre = 'Botanas') AS cat,'PIEZA' AS um,0 AS permite_dec,22.00 AS precio,
         'https://picsum.photos/seed/sab001/400/300' AS img UNION ALL
  SELECT 'SAB002','Sabritas Adobadas 170g','Papas fritas sabor adobadas 170g',(SELECT id FROM categorias WHERE nombre = 'Botanas'),'PIEZA',0,22.00,
         'https://picsum.photos/seed/sabritas-adobadas/400/300' UNION ALL
  SELECT 'SAB003','Takis Fuego 90g','Takis Fuego sabor intenso 90g',(SELECT id FROM categorias WHERE nombre = 'Botanas'),'PIEZA',0,20.00,
         'https://picsum.photos/seed/takis-fuego/400/300' UNION ALL
  SELECT 'SAB004','Cheetos Torciditos 190g','Cheetos torcidos sabor queso 190g',(SELECT id FROM categorias WHERE nombre = 'Botanas'),'PIEZA',0,24.00,
         'https://picsum.photos/seed/cheetos-torciditos/400/300'
) s
JOIN categorias c ON c.id = s.cat;

COMMIT;

-- ---------------------------------------------------------------------
-- 2. REFRESCOS (2 productos)
-- ---------------------------------------------------------------------
INSERT INTO productos (plu, nombre, descripcion, categoria_id, unidad_medida, permite_decimales, precio_base, es_compuesto, activo, imagen_url)
SELECT s.plu, s.nombre, s.descripcion, c.id, s.um, s.permite_dec, s.precio, 0, 1, s.img
FROM (
  SELECT 'COC001' AS plu,'Coca-Cola 600ml' AS nombre,'Coca-Cola original 600ml' AS descripcion,(SELECT id FROM categorias WHERE nombre = 'Refrescos') AS cat,'PIEZA' AS um,0 AS permite_dec,18.00 AS precio,
         'https://picsum.photos/seed/coca-cola-600ml/400/300' AS img UNION ALL
  SELECT 'PEC001','Pepsi 600ml','Pepsi cola 600ml',(SELECT id FROM categorias WHERE nombre = 'Refrescos'),'PIEZA',0,17.00,
         'https://picsum.photos/seed/pepsi-600ml/400/300'
) s
JOIN categorias c ON c.id = s.cat;

COMMIT;

-- ---------------------------------------------------------------------
-- 3. CERVEZAS (2 productos)
-- ---------------------------------------------------------------------
INSERT INTO productos (plu, nombre, descripcion, categoria_id, unidad_medida, permite_decimales, precio_base, es_compuesto, activo, imagen_url)
SELECT s.plu, s.nombre, s.descripcion, c.id, s.um, s.permite_dec, s.precio, 0, 1, s.img
FROM (
  SELECT 'CER001' AS plu,'Corona Extra 355ml' AS nombre,'Cerveza Corona Extra 355ml' AS descripcion,(SELECT id FROM categorias WHERE nombre = 'Cervezas') AS cat,'PIEZA' AS um,0 AS permite_dec,28.00 AS precio,
         'https://picsum.photos/seed/corona-extra-355ml/400/300' AS img UNION ALL
  SELECT 'CER002','Modelo Especial 355ml','Cerveza Modelo Especial 355ml',(SELECT id FROM categorias WHERE nombre = 'Cervezas'),'PIEZA',0,28.00,
         'https://picsum.photos/seed/modelo-especial-355ml/400/300'
) s
JOIN categorias c ON c.id = s.cat;

COMMIT;

-- ---------------------------------------------------------------------
-- 4. ENLATADOS (2 productos)
-- ---------------------------------------------------------------------
INSERT INTO productos (plu, nombre, descripcion, categoria_id, unidad_medida, permite_decimales, precio_base, es_compuesto, activo, imagen_url)
SELECT s.plu, s.nombre, s.descripcion, c.id, s.um, s.permite_dec, s.precio, 0, 1, s.img
FROM (
  SELECT 'ENL001' AS plu,'Frijoles Negros La Costena 430g' AS nombre,'Frijoles negros enteros La Costena 430g' AS descripcion,(SELECT id FROM categorias WHERE nombre = 'Enlatados') AS cat,'PIEZA' AS um,0 AS permite_dec,24.00 AS precio,
         'https://picsum.photos/seed/frijoles-negros/400/300' AS img UNION ALL
  SELECT 'ENL002','Atun Dolores 140g','Atun Dolores en aceite 140g',(SELECT id FROM categorias WHERE nombre = 'Enlatados'),'PIEZA',0,32.00,
         'https://picsum.photos/seed/atun-dolores/400/300'
) s
JOIN categorias c ON c.id = s.cat;

COMMIT;

-- ---------------------------------------------------------------------
-- 5. LACTEOS (2 productos)
-- ---------------------------------------------------------------------
INSERT INTO productos (plu, nombre, descripcion, categoria_id, unidad_medida, permite_decimales, precio_base, es_compuesto, activo, imagen_url)
SELECT s.plu, s.nombre, s.descripcion, c.id, s.um, s.permite_dec, s.precio, 0, 1, s.img
FROM (
  SELECT 'LAC001' AS plu,'Leche Lala Entera 1L' AS nombre,'Leche Lala entera 1 litro' AS descripcion,(SELECT id FROM categorias WHERE nombre = 'Lacteos') AS cat,'LITRO' AS um,0 AS permite_dec,26.00 AS precio,
         'https://picsum.photos/seed/leche-lala-1l/400/300' AS img UNION ALL
  SELECT 'LAC002','Queso Oaxaca 200g','Queso Oaxaca Lala 200g',(SELECT id FROM categorias WHERE nombre = 'Lacteos'),'PIEZA',0,68.00,
         'https://picsum.photos/seed/queso-oaxaca-200g/400/300'
) s
JOIN categorias c ON c.id = s.cat;

COMMIT;

-- ---------------------------------------------------------------------
-- 7. Lotes con fechas de caducidad (alimenta la alerta de caducidad)
-- ---------------------------------------------------------------------
INSERT INTO lotes (producto_id, codigo_lote, costo_compra, cantidad_inicial, cantidad_actual, fecha_recepcion, fecha_caducidad)
SELECT p.id, s.lote, s.costo, s.cant, s.cant, s.recepcion, s.caducidad
FROM (
  SELECT 'SAB001' AS plu,'SAB-24-001' AS lote,15.00 AS costo,20 AS cant,DATE_SUB(CURDATE(), INTERVAL 10 DAY) AS recepcion,DATE_ADD(CURDATE(), INTERVAL 4 MONTH) AS caducidad UNION ALL
  SELECT 'COC001','COC-24-001',12.00,80,DATE_SUB(CURDATE(), INTERVAL 5 DAY),DATE_ADD(CURDATE(), INTERVAL 8 MONTH) UNION ALL
  SELECT 'CER001','CER-24-001',18.00,60,DATE_SUB(CURDATE(), INTERVAL 10 DAY),DATE_ADD(CURDATE(), INTERVAL 10 MONTH) UNION ALL
  SELECT 'ENL001','ENL-24-001',16.00,40,DATE_SUB(CURDATE(), INTERVAL 30 DAY),DATE_ADD(CURDATE(), INTERVAL 24 MONTH) UNION ALL
  SELECT 'LAC001','LAC-24-001',19.00,50,DATE_SUB(CURDATE(), INTERVAL 5 DAY),DATE_ADD(CURDATE(), INTERVAL 10 DAY)
) s
JOIN productos p ON p.plu = s.plu;

COMMIT;
