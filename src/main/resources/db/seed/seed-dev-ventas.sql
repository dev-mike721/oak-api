-- =====================================================================
-- seed-dev-ventas.sql — Historial de ventas para los dashboards
-- =====================================================================
-- Uso:  mysql -uroot -p<pass> tienda_db < src/main/resources/db/seed/seed-dev-ventas.sql
--
-- REQUIERE seed-dev.sql aplicado antes.
-- También fuera de db/migration: Flyway nunca lo ejecuta.
--
-- Genera 35 días de operación: turnos abiertos y cerrados por día, ventas
-- en efectivo, tarjeta y crédito, movimientos de inventario y pagos aplicados.
-- Al final deja saldo pendiente real en varios clientes para que la cartera
-- vencida y la cuenta corriente tengan qué mostrar.
-- =====================================================================

SET @hay_ventas := (SELECT COUNT(*) FROM ventas);
DROP TEMPORARY TABLE IF EXISTS _abort2;
CREATE TEMPORARY TABLE _abort2 (x INT NOT NULL CHECK (x = 0));
INSERT INTO _abort2 SELECT IF(@hay_ventas = 0, 0, 1);
DROP TEMPORARY TABLE _abort2;

-- ---------------------------------------------------------------------
-- Turnos de los últimos 35 días (cerrados) + el turno de hoy (abierto)
-- Se generan 2 por día (uno por caja) y se marcan cerrados con su cuadre.
-- ---------------------------------------------------------------------
INSERT INTO turnos_caja
  (usuario_id, caja_id, solicitado_por_id, autorizado_por_id,
   fecha_apertura, monto_inicial, fecha_cierre, monto_fisico_conteo,
   monto_sistema_calculado, diferencia, estado, estado_solicitud, autorizado_en)
SELECT
  u.id, c.id, u.id, admin.id,
  TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL d.dias DAY), SEC_TO_TIME(32400 + 1800 * (c.id % 2))),
  500.00,
  TIMESTAMP(DATE_ADD(DATE_SUB(CURDATE(), INTERVAL d.dias DAY), INTERVAL 9 HOUR), SEC_TO_TIME(32400 + 1800 * (c.id % 2))),
  500.00 + 1800.00 - (d.dias * 7 % 5),
  500.00 + 1800.00,
  -1 * (d.dias * 7 % 5),
  'CERRADO', 'AUTORIZADO',
  TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL d.dias DAY), SEC_TO_TIME(32000))
FROM (SELECT 0 dias UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
      UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
      UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12 UNION ALL SELECT 13 UNION ALL SELECT 14
      UNION ALL SELECT 15 UNION ALL SELECT 16 UNION ALL SELECT 17 UNION ALL SELECT 18 UNION ALL SELECT 19
      UNION ALL SELECT 20 UNION ALL SELECT 21 UNION ALL SELECT 22 UNION ALL SELECT 23 UNION ALL SELECT 24
      UNION ALL SELECT 25 UNION ALL SELECT 26 UNION ALL SELECT 27 UNION ALL SELECT 28 UNION ALL SELECT 29
      UNION ALL SELECT 30 UNION ALL SELECT 31 UNION ALL SELECT 32 UNION ALL SELECT 33 UNION ALL SELECT 34) d
CROSS JOIN caja c
JOIN usuarios u ON u.username = IF(c.nombre = 'Caja 1 - Mostrador', 'lucia', 'marco')
JOIN usuarios admin ON admin.username = 'rosa';

-- ---------------------------------------------------------------------
-- Ventas: 4 por caja y día (EFECTIVO, TARJETA, EFECTIVO, CREDITO)
-- ---------------------------------------------------------------------
INSERT INTO ventas (uuid, turno_id, usuario_id, cliente_id, caja_id, fecha_hora,
                    metodo_pago, total, saldo_pendiente, fecha_vencimiento, estado, sincronizado)
SELECT
  UUID(),
  t.id,
  t.usuario_id,
  cl.id,
  t.caja_id,
  TIMESTAMP(DATE(t.fecha_apertura), SEC_TO_TIME(34000 + 1500 * i.n + 700 * (t.caja_id % 2))),
  i.pago,
  180.00 + ((DAY(t.fecha_apertura) * 37 + i.n * 53) % 420),
  CASE WHEN i.pago = 'CREDITO' THEN 180.00 + ((DAY(t.fecha_apertura) * 37 + i.n * 53) % 420) ELSE 0.00 END,
  CASE WHEN i.pago = 'CREDITO' THEN DATE_ADD(DATE(t.fecha_apertura), INTERVAL 30 DAY) ELSE NULL END,
  'COMPLETADA',
  1
FROM turnos_caja t
JOIN (SELECT 1 n, 'EFECTIVO' pago
      UNION ALL SELECT 2, 'TARJETA'
      UNION ALL SELECT 3, 'EFECTIVO'
      UNION ALL SELECT 4, 'CREDITO') i ON 1 = 1
JOIN (SELECT id, ROW_NUMBER() OVER (ORDER BY id) rn FROM cliente) cl
  ON cl.rn = (((i.n + (t.caja_id % 2) * 3 + DAY(t.fecha_apertura)) - 1) % 15) + 1
WHERE t.estado = 'CERRADO';

-- ---------------------------------------------------------------------
-- Un turno abierto de hoy, para poder operar
-- ---------------------------------------------------------------------
INSERT INTO turnos_caja
  (usuario_id, caja_id, solicitado_por_id, autorizado_por_id,
   fecha_apertura, monto_inicial, estado, estado_solicitud, autorizado_en)
SELECT u.id, c.id, u.id, admin.id,
       TIMESTAMP(CURDATE(), '07:00:00'), 500.00,
       'ABIERTO', 'AUTORIZADO', NOW()
FROM caja c
JOIN usuarios u ON u.username = IF(c.nombre = 'Caja 1 - Mostrador', 'lucia', 'marco')
JOIN usuarios admin ON admin.username = 'rosa'
WHERE c.nombre = 'Caja 1 - Mostrador';

-- ---------------------------------------------------------------------
-- Detalle de ventas: se reparte el total entre 8 productos reales
-- ---------------------------------------------------------------------
INSERT INTO detalle_ventas (venta_uuid, producto_id, lote_id, cantidad, precio_unitario_aplicado, subtotal)
SELECT v.uuid, p.id, l.id, x.cant, x.precio, ROUND(x.cant * x.precio, 2)
FROM ventas v
JOIN (
  SELECT 1 n, 'SAB001' plu, 2.000 cant, 22.00 precio UNION ALL
  SELECT 2, 'LAC001', 3.000, 26.00 UNION ALL
  SELECT 3, 'PAN001', 1.000, 58.00 UNION ALL
  SELECT 4, 'CAR6001', 1.500, 139.00 UNION ALL
  SELECT 5, 'FRV5003', 2.500, 46.00 UNION ALL
  SELECT 6, 'LIM7001', 1.000, 68.00 UNION ALL
  SELECT 7, 'BEB2001', 6.000, 18.00 UNION ALL
  SELECT 8, 'HIG8002', 1.000, 118.00
) x ON x.n = (v.total DIV 250) + 1
JOIN productos p ON p.plu = x.plu
LEFT JOIN lotes l ON l.producto_id = p.id
WHERE l.id IS NOT NULL
GROUP BY v.uuid, p.id, l.id, x.cant, x.precio;

-- ---------------------------------------------------------------------
-- Saldo pendiente real en los clientes de crédito
-- ---------------------------------------------------------------------
UPDATE cliente cl
JOIN (SELECT cliente_id, SUM(saldo_pendiente) total FROM ventas
      WHERE metodo_pago = 'CREDITO' AND estado = 'COMPLETADA'
      GROUP BY cliente_id) v ON v.cliente_id = cl.id
SET cl.saldo_pendiente = v.total;

-- ---------------------------------------------------------------------
-- Pagos de crédito ya aplicados (para que la cuenta corriente tenga historial)
-- ---------------------------------------------------------------------
INSERT INTO pago_credito (cliente_id, monto, fecha, metodo_pago, referencia, aplicado,
                          monto_restante, usuario_id, anulado, created_at, updated_at)
SELECT
  v.cliente_id,
  ROUND(v.saldo_pendiente / 2, 2),
  DATE_ADD(v.fecha_hora, INTERVAL 2 DAY),
  'EFECTIVO',
  CONCAT('REF-', DATE_FORMAT(v.fecha_hora, '%Y%m%d')),
  1,
  0.00,
  v.usuario_id,
  0, NOW(), NOW()
FROM ventas v
WHERE v.metodo_pago = 'CREDITO' AND v.estado = 'COMPLETADA';

-- ---------------------------------------------------------------------
-- Movimientos de inventario por las salidas de esas ventas
-- ---------------------------------------------------------------------
INSERT INTO movimientos_inventario (lote_id, usuario_id, tipo_movimiento, cantidad,
                                   cantidad_anterior, cantidad_nueva, motivo,
                                   documento_tipo, documento_id, fecha_hora)
SELECT l.id, 2, 'SALIDA_VENTA', d.cantidad, d.cantidad * 4, d.cantidad * 3,
       'Venta de mostrador', 'VENTA', v.uuid, v.fecha_hora
FROM detalle_ventas d
JOIN ventas v ON v.uuid = d.venta_uuid
JOIN lotes l ON l.id = d.lote_id;

-- ---------------------------------------------------------------------
-- Resumen
-- ---------------------------------------------------------------------
SELECT
  (SELECT COUNT(*) FROM categorias)  AS categorias,
  (SELECT COUNT(*) FROM productos)   AS productos,
  (SELECT COUNT(*) FROM lotes)       AS lotes,
  (SELECT COUNT(*) FROM cliente)     AS clientes,
  (SELECT COUNT(*) FROM proveedores) AS proveedores,
  (SELECT COUNT(*) FROM usuarios)    AS usuarios,
  (SELECT COUNT(*) FROM caja)        AS cajas,
  (SELECT COUNT(*) FROM turnos_caja) AS turnos,
  (SELECT COUNT(*) FROM ventas)      AS ventas,
  (SELECT ROUND(COALESCE(SUM(total),0),2) FROM ventas) AS venta_total,
  (SELECT ROUND(COALESCE(SUM(saldo_pendiente),2),2) FROM ventas WHERE metodo_pago='CREDITO') AS cartera,
  (SELECT COUNT(*) FROM pago_credito) AS pagos,
  (SELECT COUNT(*) FROM detalle_ventas) AS detalles,
  (SELECT COUNT(*) FROM movimientos_inventario) AS movimientos;
