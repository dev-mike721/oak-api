# Guía de Despliegue - oak-api

## Prerrequisitos

- **MySQL 8.0+** (o MariaDB 10.11+)
- **JDK 21+** (Temurin/OpenJDK)
- **Maven 3.9+** (incluido wrapper `./mvnw`)
- Acceso de red: Puerto 8081 (API), Puerto 3306 (MySQL)

## Variables de Entorno Requeridas

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `DB_HOST` | Host de MySQL | `localhost` |
| `DB_PORT` | Puerto MySQL | `3306` |
| `DB_NAME` | Nombre de BD | `tienda_db` |
| `DB_USER` | Usuario MySQL | `root` |
| `DB_PASSWORD` | Contraseña MySQL | `secret123` |
| `JWT_SECRET` | Clave firma JWT (mín. 32 bytes, base64) | `c2VjcmV0LWtleS1taW5pbXVtLTMyLWJ5dGVzLWJhc2U2NA==` |

## Migración Flyway

```bash
# Ejecutar migraciones
./mvnw flyway:migrate

# Verificar estado
./mvnw flyway:info

# En BD limpia (primera vez)
./mvnw flyway:migrate
# Verifica: 12 migraciones aplicadas, última versión = 12
```

## Inyección de Secrets

### Systemd (Linux)
```ini
# /etc/systemd/system/oak-api.service
[Service]
Environment=DB_HOST=localhost
Environment=DB_PASSWORD=secret123
Environment=JWT_SECRET=c2VjcmV0LWtleS1taW5pbXVtLTMyLWJ5dGVzLWJhc2U2NA==
ExecStart=/usr/bin/java -jar -Dspring.profiles.active=prod /opt/oak-api/oak-api.jar
```

### Docker
```yaml
# docker-compose.yml
services:
  api:
    image: ghcr.io/usuario/oak-api:latest
    environment:
      - DB_HOST=mysql
      - DB_PASSWORD=${DB_PASSWORD}
      - JWT_SECRET=${JWT_SECRET}
      - SPRING_PROFILES_ACTIVE=prod
    ports:
      - "8081:8081"
    depends_on:
      - mysql

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: tienda_db
      MYSQL_ROOT_PASSWORD: secret123
```

### Kubernetes
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: oak-api-secrets
type: Opaque
stringData:
  DB_PASSWORD: "secret123"
  JWT_SECRET: "c2VjcmV0LWtleS1taW5pbXVtLTMyLWJ5dGVzLWJhc2U2NA=="
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: oak-api
spec:
  template:
    spec:
      containers:
      - name: api
        envFrom:
        - secretRef:
            name: oak-api-secrets
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
```

## Activación de Perfil Producción

```bash
java -Dspring.profiles.active=prod -jar oak-api.jar
```

O con variable de entorno:
```bash
SPRING_PROFILES_ACTIVE=prod java -jar oak-api.jar
```

## Verificación de Arranque

```bash
# Health check
curl http://localhost:8081/actuator/health
# Respuesta esperada: {"status":"UP", "components":{"db":"UP","diskSpace":"UP"}}

# Verificar logs JSON
tail -f logs/oak-api.log | head -5
# Formato: 2026-10-01 14:30:00 [main] INFO  com.abarrotes.oakapi.OakApiApplication - Started
```

## Verificación Funcional Rápida

```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"test1234"}' | jq -r .token)

# 2. Abrir turno
curl -X POST http://localhost:8081/api/v1/turnos/solicitar \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"cajaId":1,"montoInicial":500.00}'

# 3. Registrar venta EFECTIVO
curl -X POST http://localhost:8081/api/v1/ventas \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"metodoPago":"EFECTIVO","detalle":[{"productoId":2,"cantidad":1}]}'

# 3b. Registrar venta CREDITO
curl -X POST http://localhost:8081/api/v1/ventas \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"metodoPago":"CREDITO","clienteId":1,"detalle":[{"productoId":2,"cantidad":1}]}'

# 4. Verificar cuenta corriente
curl http://localhost:8081/api/v1/clientes/1/cuenta-corriente \
  -H "Authorization: Bearer $TOKEN"
```

## Rollback

```bash
# 1. Detener instancia nueva
systemctl stop oak-api  # o docker stop oak-api

# 2. (Opcional) Revertir Flyway si es seguro
# NOTA: Flyway no soporta rollback automático. 
# Solo revertir si la migración es reversible (DROP/CREATE sin datos).
# ./mvnw flyway:undo  # Requiere Flyway Teams/Enterprise

# 3. Desplegar JAR anterior
cp oak-api-prev.jar oak-api.jar
systemctl start oak-api  # o docker start oak-api

# 4. Verificar health
curl http://localhost:8081/actuator/health
```

## Checklist Post-Despliegue

- [ ] Health endpoint responde `UP`
- [ ] Logs en formato JSON, sin SQL
- [ ] Login y JWT funcionan
- [ ] Turno se abre correctamente
- [ ] Venta EFECTIVO registrada con impuestos
- [ ] Venta CREDITO genera `saldo_pendiente` y `fecha_vencimiento`
- [ ] Cuenta corriente muestra saldos correctos
- [ ] Métricas Prometheus en `/actuator/prometheus`
- [ ] Backups BD programados (mysqldump diario)

## Contacto

Para incidencias: revisar logs en `logs/oak-api.log` y health endpoint.