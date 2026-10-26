<div align="center">

# 🌿 Oak API

**Backend REST para el sistema de gestión de inventario de una tienda de abarrotes**

API moderna, segura y auditable para administrar productos, productos compuestos (recetas/BOM), lotes con caducidad, movimientos de inventario bajo la política **FEFO** (*First Expired, First Out*), alertas de vencimiento y valorización del stock.

</div>

---

## ✨ Características

- **Autenticación JWT** con roles (`ADMINISTRADOR`, `GERENTE`, `CAJERO`, `SUPERVISOR`) y contraseñas cifradas con BCrypt.
- **Gestión de productos** con PLU, categoría, unidad de medida (pieza, kg, gramo, litro) y soporte de decimales para productos a granel.
- **Productos compuestos**: modelado de recetas/combos a partir de productos simples, con cálculo automático de **precio sugerido**.
- **Trazabilidad por lotes**: costo de compra, cantidad inicial/actual, fecha de recepción y caducidad.
- **Consumo FEFO**: despacho automático de stock priorizando el lote con caducidad más próxima, con **bloqueo pesimista** para evitar condiciones de carrera.
- **Ajustes de inventario** manuales (positivo, negativo, merma) con validaciones de stock y auditoría completa por usuario.
- **Alertas de caducidad** con niveles semafóricos: 🔴 rojo (≤ 15 días), 🟡 amarillo (≤ 30 días), 🟢 verde (> 30 días).
- **Valorización de inventario** por categoría (cantidad actual × costo de compra).
- **Historial de movimientos** paginado y filtrable por lote, producto y tipo de movimiento.
- **Manejo global de excepciones** con respuestas de error consistentes y semántica HTTP correcta.
- **Datos de demostración** precargados (categorías, 10 productos simples y 3 combos).
- **Gestión de listas de precios** con escalones de volumen, listas por canal/cliente y precios especiales por cliente-producto.
- **Resolución de precios por prioridad**: Precio Especial > Lista Cliente > Lista Canal Default > Precio Base.
- **Cálculo de impuestos por línea** en ventas (IVA, IEPS, retenciones) con snapshot de tasas.
- **Ventas a crédito** con control de saldo pendiente, fecha de vencimiento y aplicación FIFO de pagos.
- **Presupuestos** con ciclo de vida (BORRADOR → ENVIADO → APROBADO → CONVERTIDO) y conversión a venta.
- **Series/IMEI** para productos serializados con estados (DISPONIBLE, VENDIDO, RESERVADO, GARANTÍA, DEVUELTO).
- **Turnos de caja** con apertura/cierre, autorización, conciliación y asignación a caja física.
- **Caché inteligente** en resolución de precios (Caffeine, TTL 5 min) con invalidación automática.

---

## 🛠️ Stack Tecnológico

| Capa        | Tecnología                                             |
|-------------|--------------------------------------------------------|
| Lenguaje    | [Java 21](https://openjdk.org/projects/jdk/21/)        |
| Framework   | [Spring Boot 4.1.0](https://spring.io/projects/spring-boot) |
| Persistencia| Spring Data JPA / Hibernate                            |
| Seguridad   | Spring Security + JJWT 0.12.5 (tokens JWT, BCrypt)     |
| Validación  | Jakarta Bean Validation                                |
| Base de datos | MySQL 8+ / MariaDB 10.11+ (mysql-connector-j)        |
| Migraciones | Flyway 9.22.3 (con flyway-mysql)                      |
| Cache       | Caffeine                                               |
| Build       | Maven 3.9+                                             |
| Auxiliares  | Lombok 1.18.48                                         |
| CI/CD       | GitHub Actions                                         |
| Documentación | OpenAPI 3 (springdoc-openapi 2.6)                    |

---

## 🚀 Requisitos previos

- **JDK 21** o superior
- **Maven 3.9+** (o usa el wrapper incluido `./mvnw`)
- **MySQL 8+** con una base de datos creada (`tienda_db` por defecto)

---

## ⚙️ Configuración

La configuración se encuentra en `src/main/resources/application.yml` y **debe ajustarse al entorno**, especialmente credenciales y secretos (usa variables de entorno en producción):

```yaml
server:
  port: 8081
  servlet:
    context-path: /api/v1

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/tienda_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: <USUARIO>
    password: <CONTRASEÑA>

jwt:
  secret: <SECRETO_MIN_32_CARACTERES>
  expiration-ms: 28800000   # 8 horas de turno
```

> ⚠️ **Seguridad**: nunca expongas el secreto JWT ni credenciales de la base de datos en el repositorio. Consúmelos desde variables de entorno o un secret manager.

### Perfiles disponibles

- **`default`** (desarrollo): `ddl-auto: validate`, `flyway.enabled: true`, SQL logging enabled
- **`test`**: `ddl-auto: create`, `flyway.enabled: true`, `baseline-on-migrate: true`
- **`prod`**: `ddl-auto: validate`, `flyway.enabled: true`, secretos por variables de entorno (`${JWT_SECRET}`, `${DB_PASSWORD}`, etc.), `show-sql: false`, logs JSON, actuator health expuesto, connection pooling HikariCP optimizado

---

## 📦 Instalación y ejecución

### Desarrollo
```bash
# 1. Clonar el repositorio
git clone https://github.com/open-mike721/oak-api.git
cd oak-api

# 2. Compilar y ejecutar las pruebas
./mvnw clean verify

# 3. Levantar la aplicación (perfil dev por defecto)
./mvnw spring-boot:run
```

### Producción
```bash
# Variables de entorno requeridas
export JWT_SECRET="base64-secret-min-32-chars"
export DB_PASSWORD="db-password"
export DB_HOST=localhost
export DB_NAME=tienda_db

# Ejecutar con perfil prod
./mvnw spring-boot:run -Dspring.profiles.active=prod
# O con JAR empaquetado
java -Dspring.profiles.active=prod -jar target/oak-api-0.0.1-SNAPSHOT.jar
```

> Ver **DEPLOYMENT.md** para guía completa: Flyway, secrets (systemd/Docker/K8s), healthcheck, rollback.

La API quedará disponible en `http://localhost:8081/api/v1`.

Al primer arranque, `DataInitializer` precarga datos de demostración si la base de datos está vacía.

---

## 🔐 Autenticación

Todos los endpoints (excepto `/auth/**`) requieren el header `Authorization: Bearer <token>`.

### Obtener el token

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"cajero","password":"tu-contraseña"}'
```

Respuesta:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "cajero",
  "rol": "CAJERO"
}
```

### Usar el token

```bash
curl http://localhost:8081/api/v1/productos \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## 📚 Endpoints

### Auth

| Método | Ruta           | Descripción            | Roles       |
|--------|----------------|------------------------|-------------|
| POST   | `/auth/login`  | Login y obtención de JWT | Público    |

### Productos

| Método | Ruta                       | Descripción                              | Roles                        |
|--------|----------------------------|------------------------------------------|------------------------------|
| GET    | `/productos`               | Listar todos los productos               | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/productos/{id}`          | Buscar por ID                            | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/productos/plu/{plu}`     | Buscar por código PLU                    | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/productos/categoria/{id}`| Listar productos por categoría           | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/productos/buscar?nombre=`| Buscar por nombre                        | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/productos`               | Crear producto                           | ADMINISTRADOR, GERENTE       |
| PUT    | `/productos/{id}`          | Actualizar producto                      | ADMINISTRADOR, GERENTE       |
| DELETE | `/productos/{id}`          | Eliminar producto                        | ADMINISTRADOR, GERENTE       |

### Productos compuestos (recetas)

| Método | Ruta                             | Descripción                            | Roles                        |
|--------|----------------------------------|----------------------------------------|------------------------------|
| GET    | `/productos/{padreId}/componentes` | Obtener la receta del producto compuesto | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/productos/{padreId}/componentes` | Agregar un componente a la receta      | ADMINISTRADOR, GERENTE       |
| PUT    | `/productos/{padreId}/componentes` | Reemplazar la receta completa          | ADMINISTRADOR, GERENTE       |
| DELETE | `/productos/{padreId}/componentes/{hijoId}` | Eliminar un componente        | ADMINISTRADOR, GERENTE       |
| DELETE | `/productos/{padreId}/componentes` | Vaciar la receta                       | ADMINISTRADOR, GERENTE       |

### Inventario

| Método | Ruta                                | Descripción                              | Roles                        |
|--------|-------------------------------------|------------------------------------------|------------------------------|
| GET    | `/inventario/lotes/{productoId}`    | Lotes disponibles del producto (orden FEFO) | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/inventario/ajustes`               | Registrar ajuste manual (positivo/negativo/merma) | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/inventario/alertas-caducidad?dias=30` | Alertas de caducidad con nivel semafórico | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/inventario/valorizacion`          | Valorización del inventario por categoría | ADMINISTRADOR, GERENTE       |
| GET    | `/inventario/movimientos`           | Historial paginado y filtrable           | CAJERO, ADMINISTRADOR, GERENTE |

Filtros de `/inventario/movimientos`: `loteId`, `productoId`, `tipo` (`ENTRADA_COMPRA`, `SALIDA_VENTA`, `AJUSTE_POSITIVO`, `AJUSTE_NEGATIVO`, `MERMA`) — paginación con Spring Data (`page`, `size`, `sort`).

### Listas de Precios

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/listas-precios`                 | Listar listas de precios                 | ADMINISTRADOR, GERENTE       |
| GET    | `/listas-precios/{id}`            | Obtener lista con detalle                | ADMINISTRADOR, GERENTE       |
| POST   | `/listas-precios`                 | Crear lista de precios                   | ADMINISTRADOR, GERENTE       |
| PUT    | `/listas-precios/{id}`            | Actualizar lista                         | ADMINISTRADOR, GERENTE       |
| DELETE | `/listas-precios/{id}`            | Eliminar lista                           | ADMINISTRADOR, GERENTE       |
| POST   | `/listas-precios/{id}/detalles`   | Agregar/actualizar precio de producto    | ADMINISTRADOR, GERENTE       |
| DELETE | `/listas-precios/{id}/detalles/{productoId}` | Eliminar precio de producto | ADMINISTRADOR, GERENTE |
| GET    | `/listas-precios/disponibles`     | Listas vigentes para una fecha           | ADMINISTRADOR, GERENTE       |

### Resolución de Precios

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/precios/resolver?clienteId=1&productoId=1&cantidad=10&fecha=2024-01-15` | Resolver precio según prioridad | CAJERO, ADMINISTRADOR, GERENTE |

**Prioridad**: 1) Precio Especial Cliente-Producto → 2) Lista Cliente → 3) Lista Canal Default → 4) Precio Base Producto. Soporta escalones de volumen.

### Impuestos

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/impuestos`                      | Listar impuestos                         | ADMINISTRADOR, GERENTE       |
| POST   | `/impuestos`                      | Crear impuesto (IVA, IEPS, retenciones)  | ADMINISTRADOR, GERENTE       |
| PUT    | `/impuestos/{id}`                 | Actualizar impuesto                      | ADMINISTRADOR, GERENTE       |
| DELETE | `/impuestos/{id}`                 | Eliminar impuesto                        | ADMINISTRADOR, GERENTE       |
| POST   | `/impuestos/producto/{productoId}`| Asignar impuesto a producto              | ADMINISTRADOR, GERENTE       |
| GET    | `/impuestos/producto/{productoId}`| Obtener impuestos de un producto         | ADMINISTRADOR, GERENTE       |
| DELETE | `/impuestos/producto/{productoId}/{impuestoId}` | Quitar impuesto de producto | ADMINISTRADOR, GERENTE |

### Precios Especiales por Cliente

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| POST   | `/clientes/{clienteId}/precios-especiales` | Crear precio especial cliente-producto | ADMINISTRADOR, GERENTE |
| GET    | `/clientes/{clienteId}/precios-especiales` | Listar precios especiales del cliente | ADMINISTRADOR, GERENTE |
| DELETE | `/clientes/{clienteId}/precios-especiales/{productoId}` | Eliminar precio especial | ADMINISTRADOR, GERENTE |

> **Cache invalidation**: Al crear/eliminar precio especial o modificar lista de precios, la caché del resolver se invalida automáticamente.

### Ventas

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/ventas`                         | Listar ventas con filtros                | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/ventas/{uuid}`                  | Obtener venta con detalle e impuestos    | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/ventas`                         | Registrar venta (usa resolver de precios, calcula impuestos, asigna lote FEFO) | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/ventas/{uuid}/cancelar`         | Cancelar venta (restaura stock)          | ADMINISTRADOR, GERENTE       |

**Venta a crédito**: si `metodoPago: CREDITO` → requiere `clienteId`, setea `saldoPendiente = total`, `fechaVencimiento = now + diasCredito`.

**Respuesta de venta** incluye desglose de impuestos por línea:
```json
{
  "uuid": "...",
  "detalle": [{
    "productoId": 1,
    "cantidad": 2,
    "precioUnitarioAplicado": 100.00,
    "subtotal": 200.00,
    "impuestos": [{
      "impuestoId": 1,
      "impuestoClave": "IVA16",
      "impuestoNombre": "IVA 16%",
      "base": 200.00,
      "tasa": 0.16,
      "importeTraslado": 32.00,
      "importeRetencion": 0.00
    }]
  }]
}
```

### Clientes

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/clientes`                       | Listar clientes                          | ADMINISTRADOR, GERENTE       |
| GET    | `/clientes/{id}`                  | Obtener cliente                          | ADMINISTRADOR, GERENTE       |
| POST   | `/clientes`                       | Crear cliente                            | ADMINISTRADOR, GERENTE       |
| PUT    | `/clientes/{id}`                  | Actualizar cliente                       | ADMINISTRADOR, GERENTE       |
| DELETE | `/clientes/{id}`                  | Desactivar cliente (si saldo = 0)        | ADMINISTRADOR, GERENTE       |
| GET    | `/clientes/{id}/cuenta-corriente` | Cuenta corriente (ventas pendientes + pagos) | ADMINISTRADOR, GERENTE |
| GET    | `/clientes/{id}/aging`            | Aging de saldos vencidos                 | ADMINISTRADOR, GERENTE       |

### Pagos a Crédito (FIFO)

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| POST   | `/pagos-credito`                  | Registrar pago (monto, método, referencia) | ADMINISTRADOR, GERENTE |
| POST   | `/pagos-credito/{id}/aplicar-fifo`| Aplicar FIFO a facturas vencidas         | ADMINISTRADOR, GERENTE       |
| GET    | `/pagos-credito/{id}/aplicaciones`| Ver aplicaciones del pago                | ADMINISTRADOR, GERENTE       |
| POST   | `/pagos-credito/{id}/anular`      | Anular pago (restaura saldos)            | ADMINISTRADOR, GERENTE       |
| GET    | `/pagos-credito/cliente/{clienteId}` | Historial de pagos del cliente       | ADMINISTRADOR, GERENTE       |

**FIFO**: El pago se aplica a la factura más antigua vencida primero, creando registros en `Pago_Credito_Aplicacion` con monto aplicado y fecha.

### Presupuestos

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/presupuestos`                   | Listar con filtros                       | ADMINISTRADOR, GERENTE       |
| GET    | `/presupuestos/{id}`              | Obtener presupuesto                      | ADMINISTRADOR, GERENTE       |
| POST   | `/presupuestos`                   | Crear presupuesto (BORRADOR)             | ADMINISTRADOR, GERENTE       |
| POST   | `/presupuestos/{id}/enviar`       | Cambiar a ENVIADO                        | ADMINISTRADOR, GERENTE       |
| POST   | `/presupuestos/{id}/aprobar`      | Cambiar a APROBADO (registra aprobador)  | ADMINISTRADOR, GERENTE       |
| POST   | `/presupuestos/{id}/convertir`    | Convertir a venta (requiere APROBADO, usa FEFO para lote_id) | ADMINISTRADOR, GERENTE |
| POST   | `/presupuestos/{id}/cancelar`     | Cambiar a CANCELADO                      | ADMINISTRADOR, GERENTE       |

### Series / IMEI

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| POST   | `/series/compra`                  | Registrar series en compra               | ADMINISTRADOR, GERENTE       |
| GET    | `/series/disponibles-venta?productoId=1&cantidad=5` | Obtener N series FIFO para venta | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/series/disponibles-generales?productoId=1&limite=10` | Listar series disponibles (límite) | ADMINISTRADOR, GERENTE |
| POST   | `/series/venta?productoId=1`      | Vender series específicas                | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/series/cancelar-venta/{ventaId}`| Liberar series por cancelación           | ADMINISTRADOR, GERENTE       |
| PUT    | `/series/{id}/estado`             | Cambiar estado (DISPONIBLE, VENDIDO, RESERVADO, GARANTIA, DEVUELTO) | ADMINISTRADOR, GERENTE |

### Cajas y Turnos

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/cajas`                          | Listar cajas                             | ADMINISTRADOR, GERENTE       |
| POST   | `/cajas`                          | Crear caja                               | ADMINISTRADOR, GERENTE       |
| PUT    | `/cajas/{id}`                     | Actualizar caja                          | ADMINISTRADOR, GERENTE       |
| POST   | `/cajas/{id}/activar` / `/desactivar` | Activar/desactivar caja              | ADMINISTRADOR, GERENTE       |
| POST   | `/turnos/solicitar`               | Solicitar apertura de turno              | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/turnos/{id}/autorizar`          | Autorizar turno (ADMIN/GERENTE)          | ADMINISTRADOR, GERENTE       |
| POST   | `/turnos/{id}/rechazar`           | Rechazar solicitud                       | ADMINISTRADOR, GERENTE       |
| POST   | `/turnos/{id}/cerrar`             | Cerrar turno con conteo físico           | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/turnos/actual`                  | Turno abierto del usuario actual         | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/turnos/caja/{cajaId}`           | Historial de turnos de una caja          | ADMINISTRADOR, GERENTE       |

### Compras

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/compras`                        | Listar compras                           | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/compras/{id}`                   | Obtener compra con detalle               | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/compras`                        | Registrar compra (crea lotes, actualiza stock) | ADMINISTRADOR, GERENTE |
| PUT    | `/compras/{id}`                   | Actualizar compra                        | ADMINISTRADOR, GERENTE       |
| POST   | `/compras/{id}/pagar`             | Marcar compra como pagada                | ADMINISTRADOR, GERENTE       |
| POST   | `/compras/{id}/cancelar`          | Cancelar compra                          | ADMINISTRADOR, GERENTE       |

### Proveedores

| Método | Ruta                              | Descripción                              | Roles                        |
|--------|-----------------------------------|------------------------------------------|------------------------------|
| GET    | `/proveedores`                    | Listar proveedores                       | CAJERO, ADMINISTRADOR, GERENTE |
| GET    | `/proveedores/{id}`               | Obtener proveedor                        | CAJERO, ADMINISTRADOR, GERENTE |
| POST   | `/proveedores`                    | Crear proveedor                          | ADMINISTRADOR, GERENTE       |
| PUT    | `/proveedores/{id}`               | Actualizar proveedor                     | ADMINISTRADOR, GERENTE       |
| DELETE | `/proveedores/{id}`               | Desactivar proveedor                     | ADMINISTRADOR, GERENTE       |

---

## 🗂️ Arquitectura

La aplicación sigue una **arquitectura en capas** con responsabilidades bien separadas:

```
com.abarrotes.oakapi
├── controller          # Capa REST: expone los endpoints
├── service             # Interfaz de la lógica de negocio
│   └── impl            # Implementaciones (FEFO, recetas, etc.)
├── repository          # Acceso a datos con Spring Data JPA
├── model               # Entidades JPA y enums del dominio
├── dto                 # Objetos de transferencia (request/response)
├── security            # Filtro y servicio JWT
├── config              # Seguridad, CORS, inicialización de datos
└── exception           # Excepciones de dominio y manejador global
```

### Detalles de implementación destacados

- **Consumo FEFO**: `LoteRepository.findLotesDisponiblesFEFOConLock` ordena lotes por caducidad (los que no caducan van al final) y aplica `SELECT ... FOR UPDATE` para garantizar consistencia ante concurrencia.
- **Migración de lotes**: cada movimiento registra cantidades anterior y nueva (`MovimientoInventario`), creando una pista de auditoría completa por usuario y documento de origen.
- **Recetas**: los productos compuestos no pueden ser componentes de otros y un producto no puede referenciarse a sí mismo (validaciones de negocio).
- **Resolución de precios**: servicio con caché Caffeine (TTL 5 min), invalidación automática en cambios de lista de precios o precios especiales.
- **Impuestos en venta**: se calculan por línea usando `ImpuestoService.obtenerImpuestosDeProducto()`, se guarda snapshot en `Detalle_Venta_Impuesto`.
- **Presupuesto → Venta**: al convertir, consume stock vía FEFO y asigna `lote_id` a cada `DetalleVenta`.

---

## 🧪 Pruebas

```bash
./mvnw test
```

Incluye 23 pruebas de integración que validan:
- Resolución de precios por prioridad y escalones de volumen
- Aplicación FIFO de pagos a crédito
- Ciclo de vida completo de presupuesto → venta
- Consumo FEFO de stock con lotes

---

## 🔄 CI/CD

Pipeline en GitHub Actions (`.github/workflows/ci.yml`):

| Job | Trigger | Descripción |
|-----|---------|-------------|
| `build-test` | push/PR a `main`, `develop` | Compila, testea (`./mvnw clean verify`) |
| `flyway-migrate` | push/PR a `main`, `develop` | Migra BD con MySQL service container |
| `docker-build` | push a `main` / tags | Build & push a GHCR |

Ejecutar localmente:
```bash
# Validar pipeline completo
./mvnw clean verify flyway:migrate
```

---

## 📦 Despliegue

Ver **DEPLOYMENT.md** para guía completa:
- Prerrequisitos (MySQL 8+, JDK 21, env vars)
- Flyway migration (`./mvnw flyway:migrate`)
- Inyección de secrets (systemd, Docker, K8s)
- Profile activation (`-Dspring.profiles.active=prod`)
- Healthcheck (`GET /actuator/health`)
- Rollback procedure

---

## 🛣️ Roadmap

Los siguientes módulos están contemplados en el dominio (enums/entidades ya definidos) y listos para implementarse:

- 🛒 **Compras** con `EstadoCompra` y `DocumentoOrigen.COMPRA`
- 💵 **Ventas** con `EstadoVenta`, `MetodoPago` y turnos (`EstadoTurno`)
- 🎁 **Promociones** (`TipoPromocion`)
- 📝 **Auditoría avanzada** vía `Logs_Auditoria`
- 🔍 **Cache invalidation test**: test de integración para invalidación granular
- 🔧 **Spotless/Checkstyle**: formateo automático en CI
- 🐳 **Docker multi-arch**: build para AMD64/ARM64

---

## 📄 Licencia

Proyecto privado / no definido aún. Contacta al mantenedor para más información.