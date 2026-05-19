> **Copyright (C) 2026 Mateo Valencia Ardila. Todos los derechos reservados. El código fuente de esta aplicación está protegido por las leyes de derechos de autor. Registro DNDA No. 13-108-139. Queda estrictamente prohibida su copia, distribución o modificación sin autorización expresa.**

# Guía de Inicio Rápido

Esta guía permite levantar el backend de **Messenger** y todo su ecosistema (incluyendo base de datos y herramientas) utilizando Docker de forma profesional.

## Requisitos Previos

- Tener instalado **Docker Desktop** ([Descargar aquí](https://www.docker.com/products/docker-desktop/)).
- Asegurarse de que Docker esté iniciado.

## Instalación y Ejecución

1.  **Descargar el proyecto**: Asegúrate de tener las carpetas `messenger-backend` y `messenger-frontend` en el mismo directorio raíz.
2.  **Abrir una terminal**: Navega hasta la carpeta `messenger-backend`.
3.  **Ejecutar Docker**:

```bash
    docker-compose -f docker-compose.local.yml up --build
```

4.  **Esperar a que inicie**: La primera vez puede tardar unos minutos descargando las imágenes y compilando el código.

### Desarrollo con Hot Reloading

Para desarrollo activo donde los cambios en el código se reflejan automáticamente (sin reiniciar contenedores).

1.  **Configurar Variables de Entorno (`.env`)**:
    Copia el archivo de plantilla `.env.example` en la raíz del proyecto para crear tu archivo `.env` local:
    ```bash
    cp .env.example .env
    ```
    Abre `.env` y configura tus claves reales:
    *   **Google Maps API (`GOOGLE_MAPS_API_KEY`)**: Obligatoria para renderizar los mapas de tracking en vivo de los transportistas, trazar rutas y geolocalizar direcciones.
    *   **Google Cloud Storage (`GCS_BUCKET_NAME`, `GCP_PROJECT_ID`)**: Nombre del bucket y ID del proyecto de Google Cloud para el almacenamiento en la nube de las fotos y firmas subidas.
    *   **WhatsApp API (`WHATSAPP_PHONE_NUMBER_ID`, `WHATSAPP_ACCESS_TOKEN`, etc.)**: Para enviar notificaciones automáticas.
    *   **Cloudflare Turnstile (`TURNSTILE_SECRET_KEY`)**: Para habilitar la protección anti-bots en el login.

2.  **Credenciales de Google Cloud (JSON)**:
    A diferencia del ambiente local simulado, el entorno de desarrollo (`docker-compose.dev.yml`) realiza conexiones reales a Google Cloud.
    *   Descarga el archivo de credenciales JSON de tu cuenta de servicio de Google Cloud.
    *   Guárdalo en tu equipo anfitrión exactamente en la ruta:
        `$HOME/Documentos/gcp-json/messenger-backend.json`
        *(Esta ruta del host se mapea automáticamente dentro del contenedor en modo lectura).*

3.  **Iniciar Contenedores**:
    ```bash
    docker-compose -f docker-compose.dev.yml up --build
    ```

| Servicio            | URL                     |
| ------------------- | ----------------------- |
| Frontend (Vite HMR) | `http://localhost:5173` |
| Backend API         | `http://localhost:8080` |
| PHPMyAdmin          | `http://localhost:8081` |
| Debug Remoto        | Puerto `5005`           |

## Acceso a la Aplicación

Una vez que veas que los logs se detienen y el backend dice "Started MessengerApplication", puedes acceder a:

- **Frontend**: [http://localhost](http://localhost) (Puerto 80)
- **Base de Datos (phpMyAdmin)**: [http://localhost:8081](http://localhost:8081)
  - Usuario: `root`
  - Contraseña: `secret123`
- **Documentación API (Swagger)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **Colección Postman**: El archivo `Messenger_API.postman_collection.json` está incluido en la carpeta raíz del backend para pruebas directas.
- **Salud del Sistema**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

## Credenciales de Prueba

Utiliza estas credenciales para entrar al sistema en perfil **local**:

- **Administrador**:
  - Documento: `123456`
  - Contraseña: `admin123` (Si no existe, créalo en la DB o verifica los datos iniciales).
- **Transportista**:
  - Documento: `654321`
  - Contraseña: `password123`

> [!NOTE]
> **Simulaciones (Mocks)**:
>
> - Las **Imágenes** se guardarán dentro del contenedor (carpeta `uploads`).
> - No se requiere configuración de Google Cloud para esta demostración.

## Pruebas Automatizadas

El sistema cuenta con una suite de pruebas robusta (370+ tests) que utiliza **Testcontainers**. No necesitas configurar bases de datos manualmente para los tests.

1.  Navega a: `cd messenger`
2.  Ejecuta: `./mvnw test`
    - _Nota: Requiere tener Docker iniciado._

> [!TIP]
> **Mutation Testing**: Puedes medir la efectividad de los tests con:
> `./mvnw org.pitest:pitest-maven:mutationCoverage`

## Desarrollo en Android

El proyecto utiliza **Capacitor** para ejecutarse como aplicación nativa.

### Sincronización y Ejecución

Para preparar el entorno de Android tras realizar cambios en el frontend:

1. Navega a la carpeta del frontend: `cd messenger-frontend`
2. **Sincronizar**: `npx cap sync android`
3. **Abrir en Android Studio**: `npx cap open android`

### Conexión con el Backend Local

- El emulador de Android usa la IP `10.0.2.2` para referirse al `localhost` de tu computadora.
- El proyecto ya está pre-configurado para conectar al backend en esa dirección.

---

> **Copyright (C) 2026 Mateo Valencia Ardila. Todos los derechos reservados. El código fuente de esta aplicación está protegido por las leyes de derechos de autor. Registro DNDA No. 13-108-139. Queda estrictamente prohibida su copia, distribución o modificación sin autorización expresa.**