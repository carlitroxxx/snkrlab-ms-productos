# snkrlab-ms-productos

Microservicio de catálogo del sistema **SNKRLAB**. Expone el CRUD de productos (zapatillas) y el descuento de stock, persistiendo en MySQL.

## HERRAMIENTAS

- Java 21 · Spring Boot 4.1.1
- Spring Data JPA + MySQL Connector/J
- Spring Security + OAuth2 Resource Server (JWT de Azure AD)
- Lombok
- Docker

## Modelo de datos

**Producto** (tabla `productos`)

| Campo | Tipo | Notas |
|---|---|---|
| `id` | Long | autogenerado |
| `nombre` | String | requerido |
| `marca` | String | requerido |
| `modelo` | String | requerido |
| `precio` | Integer | requerido |
| `stock` | Integer | requerido |
| `descripcion` | String | opcional |
| `imagen` | String | opcional |

## Variables de entorno

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/snkrlab_products_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true` |
| `DB_USERNAME` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Password de MySQL | `root` |
| `AZURE_ISSUER_URI` | Base del issuer de Azure AD | `https://login.microsoftonline.com/` |
| `AZURE_TENANT_ID` | Tenant ID de Azure AD | `db9d1cc0-8c32-4341-bc72-c348daf096fb` |

`spring.jpa.hibernate.ddl-auto=update`: las tablas se crean/actualizan automáticamente al levantar el servicio.

## Ejecutar en local

Requiere una instancia de MySQL corriendo (local o en Docker) con la base `snkrlab_products_db`.

```bash
./mvnw spring-boot:run
```

Corre en `http://localhost:8082`.

## Ejecutar con Docker

```bash
docker run -d --name mysql-productos \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=snkrlab_products_db \
  -p 3306:3306 mysql:8

docker build -t ms-productos:latest .
docker run -d --name ms-productos \
  -p 8082:8082 \
  -e DB_URL="jdbc:mysql://<host-mysql>:3306/snkrlab_products_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true" \
  -e AZURE_TENANT_ID="<tenant-id>" \
  ms-productos:latest
```

## Endpoints

Base path: `/api/v1/productos`

| Método | Path | Auth | Descripción |
|---|---|---|---|
| GET | `/` | No | Lista todos los productos |
| GET | `/{id}` | No | Obtiene un producto por id (404 si no existe) |
| POST | `/` | Sí | Crea un producto |
| PUT | `/{id}` | Sí | Actualiza un producto (404 si no existe) |
| DELETE | `/{id}` | Sí | Elimina un producto (404 si no existe) |
| PUT | `/{id}/stock?cantidad=N` | Sí | Reduce el stock del producto en `cantidad` unidades |

Todas las lecturas (`GET`) son públicas; el resto de operaciones requiere `Authorization: Bearer <token>`.

### Ejemplo `POST /api/v1/productos`

```json
{
  "nombre": "Air Zoom",
  "marca": "Nike",
  "modelo": "Pegasus 41",
  "precio": 89990,
  "stock": 20,
  "descripcion": "Zapatilla running",
  "imagen": "https://..."
}
```

## Consumido por

`snkrlab-ms-carrito` usa `GET /api/v1/productos/{id}` y `PUT /api/v1/productos/{id}/stock` (vía Feign, a través del API Gateway) para validar productos y descontar stock en el checkout.

## Seguridad

- CORS habilitado solo para `http://localhost:5173` (ajustar en `SecurityConfig` según el origen real del frontend desplegado).

## Despliegue

Despliegue en una instancia EC2 junto a su propio contenedor MySQL, y quedar expuesto mediante rutas explícitas de AWS API Gateway (una por cada endpoint de la tabla anterior).
