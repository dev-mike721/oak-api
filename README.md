<div align="center">

# 🌿 Oak API

**Backend REST para el sistema de gestión de inventario de una tienda de abarrotes**

API moderna, segura y auditable para administrar productos, productos compuestos (recetas/BOM), lotes con caducidad, movimientos de inventario bajo la política **FEFO** (*First Expired, First Out*), alertas de vencimiento y valorización del stock.

</div>

---

## ✨ Características

- **Autenticación JWT** con roles (`ADMINISTRADOR`, `CAJERO`, `SUPERVISOR`) y contraseñas cifradas con BCrypt.
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

---

## 🛠️ Stack Tecnológico

| Capa        | Tecnología                                             |
|-------------|--------------------------------------------------------|
| Lenguaje    | [Java 17](https://openjdk.org/projects/jdk/17/)        |
| Framework   | [Spring Boot 4.1.0](https://spring.io/projects/spring-boot) |
| Persistencia| Spring Data JPA / Hibernate                            |
| Seguridad   | Spring Security + JJWT 0.12.5 (tokens JWT, BCrypt)     |
| Validación  | Jakarta Bean Validation                                |
| Base de datos | MySQL (mysql-connector-j)                            |
| Build       | Maven                                                  |
| Auxiliares  | Lombok                                                 |

---

## 🚀 Requisitos previos

- **JDK 17** o superior
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

---

## 📦 Instalación y ejecución

```bash
# 1. Clonar el repositorio
git clone https://github.com/open-mike721/oak-api.git
cd oak-api

# 2. Compilar y ejecutar las pruebas
./mvnw clean verify

# 3. Levantar la aplicación
./mvnw spring-boot:run
```

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

| Método | Ruta           | Descripción            |
|--------|----------------|------------------------|
| POST   | `/auth/login`  | Login y obtención de JWT |

### Productos

| Método | Ruta                       | Descripción                              |
|--------|----------------------------|------------------------------------------|
| GET    | `/productos`               | Listar todos los productos               |
| GET    | `/productos/{id}`          | Buscar por ID                            |
| GET    | `/productos/plu/{plu}`     | Buscar por código PLU                    |
| GET    | `/productos/categoria/{id}`| Listar productos por categoría           |
| GET    | `/productos/buscar?nombre=`| Buscar por nombre                        |
| POST   | `/productos`               | Crear producto                           |
| PUT    | `/productos/{id}`          | Actualizar producto                      |
| DELETE | `/productos/{id}`          | Eliminar producto                        |

### Productos compuestos (recetas)

| Método | Ruta                             | Descripción                            |
|--------|----------------------------------|----------------------------------------|
| GET    | `/productos/{padreId}/componentes` | Obtener la receta del producto compuesto |
| POST   | `/productos/{padreId}/componentes` | Agregar un componente a la receta      |
| PUT    | `/productos/{padreId}/componentes` | Reemplazar la receta completa          |
| DELETE | `/productos/{padreId}/componentes/{hijoId}` | Eliminar un componente        |
| DELETE | `/productos/{padreId}/componentes` | Vaciar la receta                       |

### Inventario

| Método | Ruta                                | Descripción                              |
|--------|-------------------------------------|------------------------------------------|
| GET    | `/inventario/lotes/{productoId}`    | Lotes disponibles del producto (orden FEFO) |
| POST   | `/inventario/ajustes`               | Registrar ajuste manual (positivo/negativo/merma) |
| GET    | `/inventario/alertas-caducidad?dias=30` | Alertas de caducidad con nivel semafórico |
| GET    | `/inventario/valorizacion`          | Valorización del inventario por categoría |
| GET    | `/inventario/movimientos`           | Historial paginado y filtrable           |

Filtros de `/inventario/movimientos`: `loteId`, `productoId`, `tipo` (`ENTRADA_COMPRA`, `SALIDA_VENTA`, `AJUSTE_POSITIVO`, `AJUSTE_NEGATIVO`, `MERMA`) — paginación con Spring Data (`page`, `size`, `sort`).

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

---

## 🧪 Pruebas

```bash
./mvnw test
```

Incluye pruebas de integración que validan el consumo de stock bajo la política FEFO y sus reglas de negocio (stock insuficiente, orden de caducidad, etc.).

---

## 🛣️ Roadmap

Los siguientes módulos están contemplados en el dominio (enums ya definidos) y listos para implementarse:

- 🛒 **Compras** con `EstadoCompra` y `DocumentoOrigen.COMPRA`
- 💵 **Ventas** con `EstadoVenta`, `MetodoPago` y turnos (`EstadoTurno`)
- 🎁 **Promociones** (`TipoPromocion`)
- 📝 **Auditoría avanzada** vía `Logs_Auditoria`

---

## 📄 Licencia

Proyecto privado / no definido aún. Contacta al mantenedor para más información.