# 🚀 Messenger Backend API

**🇪🇸 Español** | **[🇺🇸 English](./README_EN.md)**

Sistema de gestión de entregas y mensajería para operaciones de tránsito. Backend REST API con reconocimiento automático de placas vehiculares mediante OCR.

## 📋 Tabla de Contenidos

- [Arquitectura](#-arquitectura)
- [Stack Tecnológico](#-stack-tecnológico)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [API Endpoints](#-api-endpoints)
- [Esquema de Base de Datos](#-esquema-de-base-de-datos)
- [Flujo de Estados](#-flujo-de-estados)
- [Seguridad](#-seguridad)
- [Configuración e Instalación](#️-configuración-e-instalación)
- [Colección Postman](#-colección-postman)

---

## 🏗 Arquitectura

El proyecto implementa **Arquitectura Hexagonal (Ports & Adapters)** para mantener el dominio aislado de las dependencias externas.

```mermaid
graph TB
    subgraph "Adapter Layer"
        subgraph "Input Adapters"
            REST[REST Controllers]
            VAL[Validators]
            BUILD[Builders]
        end
        subgraph "Output Adapters"
            PERSIST[JPA Persistence]
            OCR[Google Vision OCR]
            STORAGE[Google Cloud Storage]
            SEC[JWT Security]
            MAPS[Google Maps]
            TRACKING[Location Tracking]
        end
    end
    
    subgraph "Application Layer"
        UC[Use Cases]
        EXC[Exceptions]
    end
    
    subgraph "Domain Layer"
        MOD[Models]
        PORTS[Ports]
        SVC[Domain Services]
    end
    
    REST --> UC
    UC --> PORTS
    PORTS --> PERSIST
    PORTS --> OCR
    PORTS --> STORAGE
    PORTS --> SEC
    PORTS --> MAPS
    PORTS --> TRACKING
    UC --> SVC
    SVC --> MOD
```

---

## 💻 Stack Tecnológico

| Componente | Tecnología |
|------------|------------|
| **Framework** | Spring Boot 4.0.0 |
| **Lenguaje** | Java 21 |
| **Base de Datos** | MySQL 8.0+ |
| **Cache/Streaming** | Redis |
| **Seguridad** | JWT + BCrypt |
| **OCR** | Google Cloud Vision API |
| **Almacenamiento** | Google Cloud Storage |
| **Mapas** | Google Maps Platform |
| **Tiempo Real** | WebSocket + Redis |
| **Build** | Maven 3.9+ |
| **Validación** | Spring Validation |

---

## 📁 Estructura del Proyecto

```
messenger/
├── src/main/java/app/
│   ├── MessengerApplication.java
│   ├── adapter/
│   │   ├── in/                          # Adaptadores de entrada
│   │   │   ├── builder/                 # Constructores de objetos
│   │   │   ├── rest/
│   │   │   │   ├── controllers/         # 5 REST Controllers
│   │   │   │   ├── mapper/              # Mappers Request/Response
│   │   │   │   ├── request/             # DTOs de entrada
│   │   │   │   └── response/            # DTOs de salida
│   │   │   └── validators/              # Validadores de entrada
│   │   └── out/                         # Adaptadores de salida
│   │       ├── maps/                    # Google Maps Integration
│   │       ├── ocr/                     # Google Vision OCR
│   │       ├── persistence/             # JPA Adapters
│   │       ├── security/                # JWT Adapter
│   │       ├── storage/                 # Google Cloud Storage
│   │       └── tracking/                # Location Tracking
│   ├── application/
│   │   ├── exceptions/                  # BusinessException, InputsException
│   │   └── usecase/                     # 4 Use Cases
│   ├── domain/
│   │   ├── model/                       # 7 Modelos de dominio + enums
│   │   ├── ports/                       # 7 Puertos (interfaces)
│   │   └── services/                    # 15 Servicios de dominio
│   └── infrastructure/
│       ├── persistence/
│       │   ├── entities/                # 7 Entidades JPA
│       │   ├── mapper/                  # Entity ↔ Domain mappers
│       │   └── repository/              # Spring Data Repositories
│       └── security/                    # SecurityConfig, JwtFilter
└── src/main/resources/
    └── application.properties
```

---

## 🔌 API Endpoints

### Autenticación (`/auth`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/login` | Iniciar sesión | 🔓 Público |

### Empleados (`/employees`) - Solo ADMIN

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/employees` | Crear empleado |
| `GET` | `/employees` | Listar todos |
| `GET` | `/employees/{id}` | Obtener por ID |
| `PUT` | `/employees/{id}` | Actualizar |
| `DELETE` | `/employees/{id}` | Eliminar |

### Concesionarios (`/dealerships`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/dealerships` | Crear | ADMIN |
| `GET` | `/dealerships` | Listar | Autenticado |
| `GET` | `/dealerships/{id}` | Obtener por ID | Autenticado |
| `PUT` | `/dealerships/{id}` | Actualizar | ADMIN |
| `DELETE` | `/dealerships/{id}` | Eliminar | ADMIN |

### Servicios de Entrega (`/services`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/services/create` | Crear servicio (multipart: image, dealershipId, messengerDocument, manualPlateNumber) |
| `PUT` | `/services/{id}/status` | Actualizar estado (multipart: status, observation, signature, photos) |
| `GET` | `/services` | Listar todos (ADMIN) o propios (MESSENGER) |
| `GET` | `/services/{id}` | Obtener por ID |
| `GET` | `/services/messenger/{doc}` | Filtrar por mensajero |
| `GET` | `/services/dealership/{id}` | Filtrar por concesionario |
| `GET` | `/services/status/{status}` | Filtrar por estado |

### Archivos (`/api/files`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `GET` | `/api/files/{filename}` | Obtener archivo | 🔓 Público |

### Ubicaciones y Rutas (`/locations`)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/locations/geocode` | Convertir dirección a coordenadas |
| `POST` | `/locations/route` | Calcular ruta optimizada para múltiples destinos |
| `GET` | `/locations/distance` | Calcular distancia entre dos puntos |
| `GET` | `/locations/reverse` | Convertir coordenadas a dirección (reverse geocoding) |

### Tracking en Tiempo Real (`/api/tracking`)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/api/tracking/update` | Actualizar ubicación del mensajero | MESSENGER/ADMIN |
| `GET` | `/api/tracking/messenger/{id}` | Obtener última ubicación de mensajero | ADMIN |
| `GET` | `/api/tracking/active` | Listar todos los mensajeros activos | ADMIN |
| `GET` | `/api/tracking/history/{id}` | Historial de tracking por fecha | MESSENGER/ADMIN |
| `GET` | `/api/tracking/service/{id}` | Historial de tracking por servicio | MESSENGER/ADMIN |

---

## 🗄 Esquema de Base de Datos

```mermaid
erDiagram
    employees {
        Long id_employee PK
        Long document UK
        String full_name
        String phone
        String user_name UK
        String password
        Role role
    }
    
    dealerships {
        Long id_dealership PK
        String name UK
        String address
        String phone
        String zone
    }
    
    plates {
        Long id_plate PK
        String plate_number UK
        PlateType plate_type
        LocalDateTime upload_date
    }
    
    service_deliveries {
        Long id_service_delivery PK
        Long plate_id FK
        Long dealership_id FK
        Long messenger_id FK
        Status current_status
        String observation
        Long signature_id FK
        LocalDateTime created_at
    }
    
    signatures {
        Long id_signature PK
        String file_path
    }
    
    photos {
        Long id_photo PK
        String file_path
        PhotoType photo_type
        Long service_delivery_id FK
        Long status_history_id FK
    }
    
    status_history {
        Long id_status_history PK
        Status previous_status
        Status new_status
        LocalDateTime change_date
        Long changed_by_employee_id FK
        Long service_delivery_id FK
    }
    
    employees ||--o{ service_deliveries : "delivers"
    dealerships ||--o{ service_deliveries : "receives"
    plates ||--o{ service_deliveries : "has"
    service_deliveries ||--o| signatures : "has"
    service_deliveries ||--o{ photos : "has"
    service_deliveries ||--o{ status_history : "tracks"
    employees ||--o{ status_history : "changes"
    status_history ||--o{ photos : "evidence"
```

### Enums

**Role:** `ADMIN`, `MESSENGER`

**PlateType:** `CAR` (ABC 123), `MOTORCYCLE` (ABC 12A), `MOTORCAR` (123 ABC)

**Status:** `ASSIGNED`, `PENDING`, `DELIVERED`, `FAILED`, `RETURNED`, `CANCELED`, `OBSERVED`, `RESOLVED`

---

## 📡 Tracking en Tiempo Real

El sistema implementa tracking GPS en vivo usando **Redis** + **WebSocket** para monitoreo de mensajeros.

### Características:
- 🔴 **Ubicación en vivo**: Actualización cada 30 segundos
- 📍 **Validación de entrega**: Radio máximo de 200 metros del concesionario
- 📊 **Historial completo**: Retención de 30 días de trayectorias
- ⚡ **Baja latencia**: Redis para caché de ubicaciones activas
- 🌐 **WebSocket**: Notificaciones push en tiempo real

### Integración Google Maps:
- **Geocoding**: Conversión dirección ↔ coordenadas
- **Directions API**: Cálculo de rutas optimizadas
- **Distance Matrix**: Estimación de tiempos de llegada
- **Reverse Geocoding**: Obtener dirección desde coordenadas

---

## 🔄 Flujo de Estados

```mermaid
stateDiagram-v2
    [*] --> ASSIGNED: Placa registrada
    ASSIGNED --> PENDING: Mensajero inicia
    PENDING --> DELIVERED: Entrega exitosa
    PENDING --> FAILED: Entrega fallida
    PENDING --> RETURNED: Devolución
    DELIVERED --> OBSERVED: Admin observa
    FAILED --> OBSERVED: Admin observa
    RETURNED --> OBSERVED: Admin observa
    OBSERVED --> RESOLVED: Resolución final
    ASSIGNED --> CANCELED: Admin cancela
    PENDING --> CANCELED: Admin cancela
```

### Requisitos de Evidencia

| Estado | Firma | Fotos | Observación |
|--------|:-----:|:-----:|:-----------:|
| `DELIVERED` | ✅ Requerida | ⚪ Opcional | ⚪ Opcional |
| `PENDING` | ✅ Requerida | ✅ Requeridas | ✅ Requerida |
| `FAILED` | ✅ Requerida | ✅ Requeridas | ✅ Requerida |
| `RETURNED` | ✅ Requerida | ✅ Requeridas | ✅ Requerida |
| `CANCELED` | ⚪ No aplica | ⚪ No aplica | ⚪ No aplica |
| `OBSERVED` | ⚪ No aplica | ⚪ No aplica | ⚪ No aplica |

---

## 🔐 Seguridad

### Autenticación JWT
- Tokens firmados con HMAC-SHA256
- Expiración: 30 minutos
- Header: `Authorization: Bearer <token>`

### Roles y Permisos
- **ADMIN**: Acceso completo a todos los endpoints
- **MESSENGER**: Solo puede ver/gestionar sus propios servicios

### CORS
Orígenes permitidos (desarrollo):
- `http://localhost:3000` (React)
- `http://localhost:4200` (Angular)
- `http://localhost:5173` (Vite)

---

## ⚙️ Configuración e Instalación

### Prerrequisitos
- Java 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.9+
- Credenciales de Google Cloud (Vision + Storage)
- API Key de Google Maps Platform

### 1. Clonar repositorio
```bash
git clone <repository-url>
cd messenger-backend/messenger
```

### 2. Configurar Base de Datos
```sql
CREATE DATABASE messenger;
```

### 3. Configurar Redis
Redis se utiliza para el tracking en tiempo real de mensajeros.

**macOS (Homebrew):**
```bash
brew install redis
brew services start redis
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install redis-server
sudo systemctl start redis-server
```

**Verificar instalación:**
```bash
redis-cli ping
# Debe responder: PONG
```

### 4. Configurar `application.properties`
```properties
# Base de Datos
spring.datasource.url=jdbc:mysql://localhost:3306/messenger
spring.datasource.username=root
spring.datasource.password=<tu-password>

# Google Cloud Storage
google.cloud.storage.bucket-name=<tu-bucket-name>
google.cloud.storage.project-id=<tu-project-id>
google.cloud.storage.signed-url-expiration-hours=24

# Google Cloud Vision
google.cloud.vision.project-id=<tu-project-id>

# Google Maps Platform
google.maps.api.key=<tu-api-key>

# Redis (tracking en tiempo real)
spring.data.redis.host=localhost
spring.data.redis.port=6379

# WebSocket (notificaciones en tiempo real)
websocket.allowed.origins=http://localhost:3000,http://localhost:5173,http://localhost:4200

# Configuración de Tracking
tracking.update.interval=30000
tracking.max.distance.validation=200
tracking.history.retention.days=30

# JWT
jwt.secret=<tu-clave-secreta-base64>
jwt.expiration=1800000
```

### 5. Configurar Google Cloud (Vision + Storage)

#### Opción recomendada: Application Default Credentials (ADC)
```bash
# Configura ADC con tu cuenta de Google Cloud
gcloud auth application-default login

# O establece la variable de entorno
export GOOGLE_APPLICATION_CREDENTIALS=/path/to/credentials.json
```

#### Características de Google Cloud Storage:
- **URLs firmadas temporales**: Acceso seguro sin bucket público
- **Expiración automática**: Las URLs expiran después de 24 horas (configurable)
- **Almacenamiento privado**: Máxima seguridad para evidencias legales
- **Escalabilidad infinita**: No consume espacio del servidor
- **CDN global**: Entrega rápida desde cualquier ubicación

### 6. Ejecutar
```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080`

---

## 📬 Colección Postman

Importa la colección incluida para probar todos los endpoints:

📄 **[Messenger_API.postman_collection.json](./Messenger_API.postman_collection.json)**

### Características:
- ✅ Guardado automático de token JWT
- ✅ Variables de entorno preconfiguradas
- ✅ Ejemplos de payloads para todos los endpoints
- ✅ Documentación inline de cada request

### Uso:
1. Importar colección en Postman
2. Ejecutar "Login" primero (el token se guarda automáticamente)
3. Los demás endpoints usarán el token guardado

---

## 📄 Licencia

Ver archivo [LICENSE](./LICENSE) para detalles.
