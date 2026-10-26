<div align="center">

# Oak API

**El núcleo de tu punto de venta**

Backend REST para ventas, inventario, precios, crédito y caja en comercios de cualquier giro.

[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MariaDB](https://img.shields.io/badge/MariaDB-10.11+-003545?logo=mariadb&logoColor=white)](https://mariadb.org/)
[![Flyway](https://img.shields.io/badge/Flyway-9.22.3-CC0200?logo=flyway&logoColor=white)](https://flywaydb.org/)
[![Tests](https://img.shields.io/badge/tests-81-2ea44f)](.)
[![License](https://img.shields.io/badge/license-Proprietary-red)](LICENSE)

</div>

---

## Por qué Oak

La mayoría de los puntos de venta se construyen para un solo tipo de negocio. Oak separa el **núcleo transaccional** del catálogo: la misma API sirve para una papelería, una ferretería, una farmacia o una tienda de abarrotes.

- **Multi-giro real**: productos simples y compuestos, venta a granel, lotes con caducidad, series/IMEI, listas de precios y crédito conviven en el mismo modelo.
- **API-first y headless**: cualquier cliente puede consumirla. **Oak POS** es solo una de las interfaces posibles.
- **Trazabilidad**: movimientos de inventario inmutables, snapshots de precio e impuesto, folios consecutivos y auditoría por usuario.
- **Operación de mostrador**: turnos, cajas, autorizaciones, arqueo, cancelaciones con reversa y ventas a crédito.
- **Pensada para México**: RFC, régimen fiscal, catálogo de impuestos SAT y series de folios.

> Oak no es un ERP. Es el motor de ventas e inventario sobre el que se puede construir uno.

---

## Verticales

El giro no se configura con módulos separados, sino con catálogos, impuestos, listas de precios y reglas de operación.

| Giro | Qué resuelve | Capacidades clave |
|---|---|---|
| Abarrotes y conveniencia | Venta diaria, granel y crédito de barrio | Lotes y caducidad (FEFO), decimales, crédito FIFO, listas de precios |
| Papelerías | Kits escolares, cotizaciones y precios por volumen | Productos compuestos, presupuestos, escalones de volumen |
| Ferreterías | Materiales por unidad o fracción, cotización y obra | Unidades fraccionadas, presupuestos, compras, series |
| Farmacias | Control estricto de caducidad y trazabilidad | FEFO, lotes, alertas de vencimiento, valorización |
| Electrónica | Control por número de serie y garantía | Series/IMEI, estados de garantía, precios especiales |
| Refaccionarias | Catálogo amplio, proveedores y crédito | Series, compuestos, proveedores, crédito, listas |
| Alimentos y bebidas | Recetas, combos y consumo de insumos | Productos compuestos (BOM), inventario FEFO |

---

## Estado del proyecto

Backend funcional y cubierto por pruebas. La SPA **Oak POS** consume esta API para dashboard, inventario y ventas.

| Métrica | Valor |
|---|---|
| Módulos de dominio | 15 |
| Controladores REST | 18 |
| Rutas documentadas | 81 |
| Migraciones Flyway | 14 (V1, V3–V15) |
| Pruebas | 81 en 12 clases |
| Java / Spring Boot | 21 / 4.1.0 |
| Base de datos | MariaDB 10.11+ |

**Alcance actual:** núcleo listo para integración y despliegue controlado. No incluye timbrado CFDI, integración con básculas ni multi-sucursal; están en el roadmap.

---

## Capacidades

### Productos y catálogo

- PLU, nombre, descripción, categoría, unidad de medida y soporte de decimales para granel.
- Productos compuestos o recetas (BOM) con cálculo de precio sugerido.
- Imágenes de producto con almacenamiento local, validación MIME con Apache Tika y endpoints de subida, reemplazo y borrado autenticados.
- Categorías con jerarquía y valorización.

### Ventas y caja

- Venta con detalle por producto, resolución automática de precio y cálculo de impuestos por línea.
- Métodos de pago: `EFECTIVO`, `TARJETA` y `CREDITO`.
- Cancelación de venta con restauración de stock y liberación de series.
- Turnos de caja con solicitud, autorización, rechazo y cierre con conteo físico.
- Cajas físicas con activación y desactivación.

### Inventario y trazabilidad

- Lotes con costo de compra, cantidad inicial/actual, recepción y caducidad.
- Consumo **FEFO** (*First Expired, First Out*) con bloqueo pesimista para evitar condiciones de carrera.
- Ajustes manuales positivos, negativos y mermas con validación de stock.
- Alertas de caducidad por niveles y valorización por categoría.
- Historial de movimientos paginado y filtrable, sin updates ni deletes.

### Precios e impuestos

- Listas de precios por canal o cliente, con escalones de volumen.
- Precios especiales por cliente-producto.
- Resolución por prioridad: **precio especial → lista del cliente → lista del canal → precio base**.
- Caché Caffeine con invalidación automática al cambiar listas o precios especiales.
- Catálogo de impuestos SAT, asignación por producto y snapshot de tasas en cada línea de venta.

### Clientes y crédito

- RFC, régimen fiscal, datos de contacto y límite de crédito.
- Ventas a crédito con saldo pendiente y fecha de vencimiento.
- Cuenta corriente y aging de saldos.
- Pagos aplicados **FIFO** a las facturas más antiguas, con anulación y reversa.

### Compras y proveedores

- Registro de compras con detalle, creación de lotes y actualización de stock.
- Pago y cancelación de compras.
- Catálogo de proveedores con datos fiscales y días de crédito.

### Presupuestos y series

- Presupuestos con ciclo `BORRADOR → ENVIADO → APROBADO → CONVERTIDO` y cancelación.
- Conversión de presupuesto a venta con consumo FEFO.
- Series/IMEI con estados `DISPONIBLE`, `VENDIDO`, `RESERVADO`, `GARANTIA` y `DEVUELTO`.

### Seguridad y auditoría

- JWT HS256 con expiración de 8 horas y contraseñas BCrypt.
- Autorización por rol y endpoint.
- Registro de auditoría por usuario, acción y entidad.
- Manejo global de excepciones con respuestas consistentes.

---

## Arquitectura

Aplicación en capas con separación estricta entre transporte, negocio y persistencia.

```text
src/main/java/com/abarrotes/oakapi
├── controller      # Endpoints REST
├── service         # Contratos de negocio
│   └── impl        # Implementaciones (FEFO, FIFO, precios, ventas)
├── repository      # Spring Data JPA
├── model           # Entidades y enums del dominio
├── dto             # Request/Response
├── security        # JWT, filtro y autenticación
├── config          # Seguridad, CORS, OpenAPI, datos iniciales
├── converter       # Conversores de dominio (régimen fiscal)
└── exception       # Excepciones y manejador global
```

> El paquete base `com.abarrotes.oakapi` es un nombre legado. El rename a `com.oakapi` está en el roadmap y no afecta la API pública.

### Flujos críticos

**Crear venta**

```text
POST /ventas
  → validar turno, usuario y cliente
  → resolver precios (caché)
  → calcular impuestos por línea y guardar snapshot
  → validar y reservar stock FEFO
  → validar límite de crédito si aplica
  → obtener folio consecutivo
  → persistir venta, detalle e impuestos
  → confirmar salida FEFO y movimientos
  → crear pago/crédito si aplica
  → registrar auditoría
```

**Consumo FEFO**

```text
Por cada línea:
  → lotes con stock > 0 ordenados por fecha de caducidad ascendente
  → descontar cantidad del lote más próximo a vencer
  → registrar MovimientoInventario por cada lote tocado
```

**Pago a crédito FIFO**

```text
Registrar pago
  → saldo disponible = monto
  → ventas del cliente con saldo > 0, ordenadas por fecha ascendente
  → aplicar min(saldo disponible, saldo de venta)
  → registrar aplicación y actualizar saldos
  → marcar venta como completada cuando llega a cero
```

**Resolución de precios**

```text
1. Precio especial cliente-producto
2. Lista de precios del cliente
3. Lista del canal por defecto
4. Precio base del producto
```

### Decisiones de diseño

- **Snapshots**: precio e impuesto se guardan en el detalle para que cambios futuros no alteren el histórico.
- **Bloqueo pesimista**: el stock FEFO se reserva con `SELECT ... FOR UPDATE`.
- **Movimientos inmutables**: el inventario se reconstruye desde `MovimientoInventario`.
- **Soft delete**: las entidades maestras se desactivan con `activo = false`.
- **Cache con invalidación explícita**: la resolución de precios se invalida al cambiar listas o precios especiales.

---

## Stack

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | MariaDB 10.11+ (driver MariaDB 3.4.2) |
| Migraciones | Flyway 9.22.3 + flyway-mysql |
| Seguridad | Spring Security + JJWT 0.12.5, BCrypt |
| Validación | Jakarta Bean Validation |
| Caché | Caffeine |
| Documentación | springdoc-openapi 3.1.1 (Swagger UI) |
| Archivos | Apache Tika 2.9.1 |
| Build | Maven 3.9+ / wrapper `./mvnw` |
| Boilerplate | Lombok 1.18.48 |
| CI | GitHub Actions |

---

## Inicio rápido

### Requisitos

- JDK 21
- MariaDB 10.11+ o MySQL 8 (con driver JDBC compatible)
- Maven 3.9+ o el wrapper incluido
- MariaDB/MySQL escuchando en `localhost:3306`

### 1. Crear la base de datos

```bash
mariadb -u root -p -e \
  "CREATE DATABASE IF NOT EXISTS tienda_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2. Exportar variables de entorno

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=tienda_db
export DB_USER=root
export DB_PASSWORD='tu-contraseña'
export JWT_SECRET="$(openssl rand -base64 32)"
export APP_UPLOAD_DIR=./uploads/productos
```

`DB_PASSWORD` y `JWT_SECRET` son obligatorios. No hay credenciales por defecto en el repositorio.

### 3. Compilar y ejecutar

```bash
./mvnw clean verify
./mvnw spring-boot:run
```

La API queda en `http://localhost:8081/api/v1`.

- Swagger UI: `http://localhost:8081/api/v1/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/api/v1/v3/api-docs`
- Health: `http://localhost:8081/api/v1/actuator/health`

### 4. Datos de demostración (opcional)

Los seeds son manuales y **Flyway no los ejecuta**:

```bash
mariadb -u root -p tienda_db < src/main/resources/db/seed/seed-dev.sql
mariadb -u root -p tienda_db < src/main/resources/db/seed/seed-dev-ventas.sql
```

Usuarios demo del seed:

| Usuario | Contraseña | Rol |
|---|---|---|
| `rosa` | `abarrotes123` | ADMINISTRADOR |
| `jorge` | `abarrotes123` | GERENTE |
| `lucia` | `abarrotes123` | CAJERO |
| `marco` | `abarrotes123` | CAJERO |

En una instalación limpia no hay endpoint de registro. Provisiona el primer usuario con el seed o con un `INSERT` usando un hash BCrypt.

---

## Configuración

### Variables de entorno

| Variable | Descripción | Obligatoria |
|---|---|---|
| `DB_HOST` | Host de MariaDB | No (default `localhost`) |
| `DB_PORT` | Puerto de MariaDB | No (default `3306`) |
| `DB_NAME` | Nombre de la base de datos | No (default `tienda_db`) |
| `DB_USER` | Usuario de base de datos | No (default `root`) |
| `DB_PASSWORD` | Contraseña de base de datos | Sí |
| `JWT_SECRET` | Secreto de firma JWT (mínimo 32 caracteres) | Sí |
| `APP_UPLOAD_DIR` | Directorio de imágenes de producto | No (default `./uploads/productos`) |
| `SPRING_PROFILES_ACTIVE` | Perfil activo (`dev`, `test`, `prod`) | No |

### Perfiles

- **default / dev**: SQL logging activo, `ddl-auto: validate`, Flyway habilitado.
- **test**: base de datos única por ejecución, Flyway `clean` + `migrate`, uploads en `/tmp`.
- **prod**: `ddl-auto: none`, SQL logging desactivado, actuator con health/metrics, logs a archivo.

El perfil de producción está versionado en `src/main/resources/application-prod.yml` y solo contiene placeholders.

### Archivos y logs

- Imágenes: `./uploads/productos` (configurable con `APP_UPLOAD_DIR`).
- Logs: `logs/oak-api.log` (no versionado).
- Límites de subida: 5 MB por archivo, 10 MB por request.

---

## Seguridad y roles

| Rol | Alcance |
|---|---|
| `ADMINISTRADOR` | Acceso completo: configuración, compras, precios, crédito, auditoría y autorización de turnos |
| `GERENTE` | Gestión comercial y operativa: ventas, inventario, compras, precios, crédito y turnos |
| `CAJERO` | Operación de mostrador: ventas, consulta de productos, inventario y turnos propios |

- JWT firmado con HS256 y expiración de 8 horas.
- Contraseñas cifradas con BCrypt.
- Todos los endpoints requieren `Authorization: Bearer <token>`, excepto `/auth/**`, Swagger/OpenAPI, `actuator/health`, `actuator/info` y la descarga pública de la foto de producto.
- No incluye refresh tokens, 2FA ni rate limiting; están en el roadmap.

---

## API

Base URL: `http://localhost:8081/api/v1`

El contrato completo y actualizado está en Swagger. Esta tabla es solo un mapa de módulos.

| Prefijo | Módulo |
|---|---|
| `/auth` | Login y emisión de JWT |
| `/productos` | Productos, componentes y fotos |
| `/categorias` | Categorías |
| `/inventario` | Lotes, ajustes, alertas, valorización y movimientos |
| `/ventas` | Ventas y cancelaciones |
| `/compras` | Compras y recepción |
| `/proveedores` | Proveedores |
| `/clientes` | Clientes, cuenta corriente y aging |
| `/pagos-credito` | Pagos y aplicación FIFO |
| `/presupuestos` | Cotizaciones y conversión a venta |
| `/listas-precios` | Listas y escalones |
| `/precios` | Resolución de precios |
| `/impuestos` | Catálogo SAT y asignación por producto |
| `/series` | Series/IMEI |
| `/cajas` | Cajas físicas |
| `/turnos` | Apertura, autorización y cierre |
| `/auditoria` | Consulta de auditoría |

### Ejemplo de login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"rosa","password":"abarrotes123"}'
```

### Ejemplo de venta

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"rosa","password":"abarrotes123"}' | jq -r .token)

curl -X POST http://localhost:8081/api/v1/ventas \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"metodoPago":"EFECTIVO","detalle":[{"productoId":2,"cantidad":1}]}'
```

La venta requiere un turno abierto. Consulta `/turnos/solicitar` y `/turnos/{id}/autorizar` en Swagger.

---

## Modelo de datos

| Entidad | Rol |
|---|---|
| `Producto` | Catálogo, PLU, unidad, precio base y flags de compuesto |
| `ProductoCompuesto` | Receta/BOM de productos compuestos |
| `Categoria` | Clasificación y valorización |
| `Lote` | Stock por costo, recepción y caducidad |
| `MovimientoInventario` | Bitácora inmutable de entradas, salidas y ajustes |
| `Venta` / `DetalleVenta` | Venta, importes y saldo pendiente |
| `DetalleVentaImpuesto` | Snapshot de impuestos por línea |
| `Cliente` | Datos fiscales, crédito y lista de precios |
| `PagoCredito` / `PagoCreditoAplicacion` | Pagos y aplicación FIFO |
| `Compra` / `DetalleCompra` | Compras y recepción de lotes |
| `Proveedor` | Catálogo de proveedores |
| `Presupuesto` / `PresupuestoDetalle` | Cotizaciones y conversión |
| `ListaPrecio` / `ListaPrecioDetalle` | Precios por canal y volumen |
| `Impuesto` / `ProductoImpuesto` | Catálogo SAT y asignación |
| `Serie` | Series/IMEI y estados |
| `Caja` / `TurnoCaja` | Cajas, turnos y arqueo |
| `Usuario` | Credenciales y rol |
| `LogAuditoria` | Registro de acciones |

Las migraciones están en `src/main/resources/db/migration` (V1, V3–V15).

---

## Pruebas

```bash
./mvnw test
```

- 81 pruebas en 12 clases.
- Integración contra MariaDB real, no H2.
- Base de datos única por ejecución (`tienda_db_test_<uuid>`) con `Flyway.clean()` + `migrate()`.
- Uploads de prueba aislados en `/tmp/uploads/productos`.

Para ejecutar localmente necesitas MariaDB activo y `DB_PASSWORD`:

```bash
DB_PASSWORD='tu-contraseña' ./mvnw test
```

Áreas cubiertas: FEFO, FIFO, resolución de precios, invalidación de caché, ciclo de presupuestos, cierre de turnos, auditoría y subida de imágenes.

---

## CI/CD y despliegue

Pipeline en `.github/workflows/ci.yml`:

| Job | Descripción |
|---|---|
| `build-test` | Compila y ejecuta la suite contra un servicio MariaDB |
| `flyway-migrate` | Aplica migraciones sobre MySQL 8 |
| `docker-build` | Build y push a GHCR (inactivo hasta que exista `Dockerfile`) |

Ramas: `master` y `develop`.

La guía completa de despliegue está en [DEPLOYMENT.md](DEPLOYMENT.md): variables, systemd, healthcheck, rollback y checklist.

---

## Roadmap

- Timbrado CFDI 4.0 y notas de crédito.
- Integración con básculas y lectores de código de barras.
- Multi-sucursal y multi-almacén.
- `Dockerfile` e imagen multi-arch.
- Renombrar paquete base a `com.oakapi` y base de datos a `oak_db`.
- OpenAPI con metadatos, ejemplos y versión.
- Observabilidad: métricas Prometheus y trazas.
- Testcontainers para pruebas de integración.
- Refresh tokens, 2FA y rate limiting.
- Modo cajero completo en la SPA Oak POS.

---

## Licencia

**Propietaria. Todos los derechos reservados.**

Copyright (c) 2026 Jose Miguel Perez Rodriguez.

El código se comparte para revisión, desarrollo y pruebas internas por colaboradores autorizados. No se permite redistribución, sublicencia, venta ni uso comercial o de producción por terceros sin autorización escrita.

Consulta [LICENSE](LICENSE) para los términos completos. Para licenciamiento comercial, contacta al titular.

---

## Contacto

- GitHub: [@dev-mike721](https://github.com/dev-mike721)
- Repositorio: [dev-mike721/oak-api](https://github.com/dev-mike721/oak-api)
