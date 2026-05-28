</details>

---

<details>
<summary><b>🇺🇸 English Version: Secrets Management</b></summary>

---

# Secrets Management

To ensure project security and operability, different secrets are used depending on the environment (CI/CD vs. Production). This document is specific to the **Messenger Backend** repository.

## CI/CD Secrets (GitHub Actions)

The continuous integration pipeline (`maven.yml`) executes tests using ephemeral Docker containers for the database and Redis. Most configurations are "hardcoded" in `application-test.properties` to facilitate testing. 

For the **Backend Repository**, there is only ONE secret required to validate build integrity with Google Cloud:

| Secret | Description | Required in CI |
|--------|-------------|----------------|
| `GOOGLE_APPLICATION_CREDENTIALS_JSON` | GCP Service Account JSON to validate build | ✅ **YES** |

> [!NOTE]
> **Frontend Secrets:** Do NOT configure frontend secrets here. Variables like `VITE_API_URL`, `VITE_GOOGLE_MAPS_API_KEY`, `VITE_GOOGLE_MAPS_MAP_ID` and `VITE_TURNSTILE_SITE_KEY` belong exclusively to the `messenger-frontend` repository.

---

## Production Secrets (Cloud Run / Deploy)

When deploying to a real environment, you need to configure all the necessary environment variables in your cloud platform (Google Cloud Run revision settings) or your production `.env` file. 

You can find the complete list of required environment variables in the [../.env.example](../.env.example) file. Below is an analysis of the core system variables:

### 1. Database & Cache
- **`DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`**: Database connection credentials.
- **`CLOUD_SQL_CONNECTION_NAME`**: Cloud SQL Instance (if using GCP).
- **`REDIS_HOST`, `REDIS_PASSWORD`**: Redis instance host and authentication.

### 2. Security & APIs
- **`JWT_SECRET`**: High-entropy random string (64+ chars). **CRITICAL**: If leaked, tokens can be forged.
- **`TURNSTILE_SECRET_KEY`**: Cloudflare Turnstile secret key to validate tokens in the backend. (The visual widget in production uses `VITE_TURNSTILE_SITE_KEY` in the frontend).
- **`GOOGLE_MAPS_API_KEY`**: Enabled for Geocoding, Directions, and Distance Matrix API.

### 3. WhatsApp Business API & Master Key
The system uses WhatsApp Cloud API to communicate with dealers and transportists.
- **`WHATSAPP_PHONE_NUMBER_ID`**: Your WhatsApp Phone Number ID.
- **`WHATSAPP_ACCESS_TOKEN`**: Permanent Access Token.
- **`WHATSAPP_VERIFY_TOKEN`**: Verification token for Webhook setup.
- **`WHATSAPP_APP_SECRET`**: Facebook App Secret for payload signature validation.
- **`WHATSAPP_MASTER_PIN`**: The **Master Key (Llave Maestra)**. A PIN (default: `9999`) that allows an administrator or "Master Key" session to view and track chassis from **all** dealerships without restriction via the WhatsApp Bot.

---

## How to configure `GOOGLE_APPLICATION_CREDENTIALS_JSON`

This is the only complex secret required for CI. Follow these steps:

1. Go to [Google Cloud Console > IAM > Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts).
2. Create a service account (or use an existing one).
3. Go to the **Keys** tab > **Add Key** > **Create new key** > **JSON**.
4. A `.json` file will download.
5. Open the file and copy **all its content**.
6. On GitHub: `Settings` > `Secrets and actions` > `New repository secret`.
7. Name: `GOOGLE_APPLICATION_CREDENTIALS_JSON`.
8. Value: Paste the JSON content.

</details>

---

# Gestión de Secretos

Para garantizar la seguridad y operatividad del proyecto, se utilizan diferentes secretos dependiendo del entorno (CI/CD vs Producción). Este documento es específico para el repositorio de **Messenger Backend**.

## Secretos para CI/CD (GitHub Actions)

El pipeline de integración continua (`maven.yml`) ejecuta pruebas utilizando contenedores Docker efímeros para la base de datos y Redis. La mayoría de configuraciones están "hardcodeadas" en `application-test.properties` para facilitar el testing.

Para el **Repositorio Backend**, solo se requiere UN secreto para validar la integridad del build con Google Cloud:

| Secreto | Descripción | Requerido en CI |
|---------|-------------|-----------------|
| `GOOGLE_APPLICATION_CREDENTIALS_JSON` | JSON de cuenta de servicio GCP para validar compilación | ✅ **SÍ** |

> [!NOTE]
> **Secretos del Frontend:** NO configures secretos del frontend aquí. Variables como `VITE_API_URL`, `VITE_GOOGLE_MAPS_API_KEY`, `VITE_GOOGLE_MAPS_MAP_ID` y `VITE_TURNSTILE_SITE_KEY` pertenecen exclusivamente al repositorio `messenger-frontend`.

---

## Secretos para Producción (Cloud Run / Deploy)

Al desplegar en un entorno real, necesitas configurar todas las variables de entorno necesarias en tu plataforma de nube (Google Cloud Run revision settings) o en tu archivo `.env` de producción.

Puedes encontrar la lista completa de variables de entorno requeridas en el archivo enlazado [../.env.example](../.env.example). A continuación un análisis de las variables core del sistema:

### 1. Base de Datos & Caché
- **`DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`**: Credenciales de conexión a la base de datos.
- **`CLOUD_SQL_CONNECTION_NAME`**: Instancia de Cloud SQL (si usas GCP).
- **`REDIS_HOST`, `REDIS_PASSWORD`**: Host y autenticación de tu instancia Redis.

### 2. Seguridad y APIs
- **`JWT_SECRET`**: String aleatorio de alta entropía. **CRÍTICO**: Si este secreto se filtra, todos los tokens pueden ser falsificados.
- **`TURNSTILE_SECRET_KEY`**: Llave secreta de Cloudflare Turnstile para validar los tokens en el backend. (El widget visual en producción utiliza `VITE_TURNSTILE_SITE_KEY` en el frontend).
- **`GOOGLE_MAPS_API_KEY`**: Habilitada para Geocoding, Directions y Distance Matrix API en el backend.

### 3. WhatsApp Business API y Llave Maestra
El sistema utiliza WhatsApp Cloud API para la comunicación con los concesionarios y transportistas.
- **`WHATSAPP_PHONE_NUMBER_ID`**: ID del número de teléfono de WhatsApp.
- **`WHATSAPP_ACCESS_TOKEN`**: Token de acceso permanente.
- **`WHATSAPP_VERIFY_TOKEN`**: Token de verificación inventado para configurar el Webhook.
- **`WHATSAPP_APP_SECRET`**: App Secret de Facebook para validar la firma de los webhooks.
- **`WHATSAPP_MASTER_PIN`**: La **Llave Maestra (Master Key)**. Un PIN (por defecto: `9999`) que permite a un administrador iniciar sesión como "Llave Maestra" y ver / rastrear los chasis de **todos** los concesionarios sin restricción alguna mediante el Bot de WhatsApp.

---

## Cómo configurar `GOOGLE_APPLICATION_CREDENTIALS_JSON`

Este es el único secreto complejo requerido para el CI. Sigue estos pasos:

1. Ve a [Google Cloud Console > IAM > Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts).
2. Crea una cuenta de servicio (o usa una existente).
3. Ve a la pestaña **Keys** > **Add Key** > **Create new key** > **JSON**.
4. Se descargará un archivo `.json`.
5. Abre el archivo y copia **todo su contenido**.
6. En GitHub: `Settings` > `Secrets and actions` > `New repository secret`.
7. Nombre: `GOOGLE_APPLICATION_CREDENTIALS_JSON`.
8. Valor: Pega el contenido del JSON.

---
