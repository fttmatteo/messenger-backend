# 🚀 Guía de Inicio Rápido

Esta guía permite levantar el backend de **Messenger** y todo su ecosistema (incluyendo base de datos y herramientas) utilizando Docker de forma profesional.

## 📋 Requisitos Previos

- Tener instalado **Docker Desktop** ([Descargar aquí](https://www.docker.com/products/docker-desktop/)).
- Asegurarse de que Docker esté iniciado.

## 🛠 Instalación y Ejecución

1.  **Descargar el proyecto**: Asegúrate de tener las carpetas `messenger-backend` y `messenger-frontend` en el mismo directorio raíz.
2.  **Abrir una terminal**: Navega hasta la carpeta `messenger-backend`.
3.  **Ejecutar Docker**:
```bash
    docker-compose up --build
```
4.  **Esperar a que inicie**: La primera vez puede tardar unos minutos descargando las imágenes y compilando el código.

## 🌐 Acceso a la Aplicación

Una vez que veas que los logs se detienen y el backend dice "Started MessengerApplication", puedes acceder a:

-   **Frontend**: [http://localhost](http://localhost) (Puerto 80)
-   **Base de Datos (phpMyAdmin)**: [http://localhost:8081](http://localhost:8081)
    -   Usuario: `root`
    -   Contraseña: `secret123`
-   **Documentación API (Swagger)**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
-   **Colección Postman**: El archivo `Messenger_API.postman_collection.json` está incluido en la carpeta raíz del backend para pruebas directas.
-   **Salud del Sistema**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🔑 Credenciales de Prueba

Utiliza estas credenciales para entrar al sistema en perfil **local**:

-   **Administrador**:
    -   Documento: `123456`
    -   Contraseña: `admin123` (Si no existe, créalo en la DB o verifica los datos iniciales).
-   **Mensajero**:
    -   Documento: `654321`
    -   Contraseña: `password123`

> [!NOTE]
> **Simulaciones (Mocks)**: 
> - El **OCR** aceptará cualquier imagen y devolverá la placa `ABC123`.
> - Las **Imágenes** se guardarán dentro del contenedor (carpeta `uploads`).
> - No se requiere configuración de Google Cloud para esta demostración.
