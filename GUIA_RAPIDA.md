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

Para desarrollo activo donde los cambios en el código se reflejan automáticamente (sin reiniciar contenedores):

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
- **Mensajero**:
  - Documento: `654321`
  - Contraseña: `password123`

> [!NOTE]
> **Simulaciones (Mocks)**:
>
> - El **OCR** aceptará cualquier imagen y devolverá la placa `ABC123`.
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