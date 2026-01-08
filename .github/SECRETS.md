</details>

---

<details>
<summary><b>🇺🇸 English Version: Secrets Management</b></summary>

---

# Secrets Management 🔐

To ensure project security and operability, different secrets are used depending on the environment (CI/CD vs. Production).

## 🤖 CI/CD Secrets (GitHub Actions)

The continuous integration pipeline (`maven.yml`) executes tests using ephemeral Docker containers for the database and Redis. Most configurations are "hardcoded" in `application-test.properties` to facilitate testing, but one secret is required to validate build integrity with Google Cloud.

| Secret | Description | Required in CI |
|--------|-------------|----------------|
| `GOOGLE_APPLICATION_CREDENTIALS_JSON` | GCP Service Account JSON to validate build | ✅ **YES** |
| `JWT_SECRET` | Token signing key | ❌ No (Uses test value) |
| `DB_PASSWORD` | Database password | ❌ No (Uses MySQL in Docker) |
| `GOOGLE_MAPS_API_KEY` | Maps Key | ❌ No (Uses mocks) |

> [!NOTE]
> The CI environment automatically spins up MySQL and Redis via Docker. You do not need to configure database credentials in GitHub Secrets for tests to pass.

---

## 🚀 Production Secrets (Cloud Run / Deploy)

When deploying to a real environment, you **DO** need to configure all the following environment variables or secrets in your cloud platform (Google Cloud Run revision settings).

### 1. Database & Cache
- **`DB_NAME`**: Database name (e.g., `messenger_prod`)
- **`DB_USERNAME`**: Connection user
- **`DB_PASSWORD`**: Strong password
- **`CLOUD_SQL_CONNECTION_NAME`**: Cloud SQL Instance (if using GCP)
- **`REDIS_HOST`**: Redis instance host
- **`REDIS_PASSWORD`**: (Optional) Redis auth

### 2. Security
- **`JWT_SECRET`**:
  - High-entropy random string (64+ chars).
  - Generate with: `openssl rand -base64 64`
  - **CRITICAL**: If this secret leaks, all tokens can be forged.

### 3. Google Services
- **`GOOGLE_MAPS_API_KEY`**: Enabled for Geocoding, Directions, and Distance Matrix API.
- **`GCS_BUCKET_NAME`**: Storage bucket name for evidence.
- **`GCP_PROJECT_ID`**: Your Google Cloud project ID.

---

## 🛠️ How to configure `GOOGLE_APPLICATION_CREDENTIALS_JSON`

This is the only complex secret required for CI. Follow these steps:

1. Go to [Google Cloud Console > IAM > Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts).
2. Create a service account (or use an existing one).
3. Go to the **Keys** tab > **Add Key** > **Create new key** > **JSON**.
4. A `.json` file will download.
5. Open the file and copy **all its content**.
6. On GitHub: `Settings` > `Secrets and actions` > `New repository secret`.
7. Name: `GOOGLE_APPLICATION_CREDENTIALS_JSON`.
8. Value: Paste the JSON content.

<details>
<summary><b>Example JSON format</b></summary>

```json
{
  "type": "service_account",
  "project_id": "your-project",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "...",
  "client_id": "...",
  "auth_uri": "...",
  "token_uri": "...",
  "auth_provider_x509_cert_url": "...",
  "client_x509_cert_url": "..."
}
```
</details>

</details>

---

# Gestión de Secretos 🔐

Para garantizar la seguridad y operatividad del proyecto, se utilizan diferentes secretos dependiendo del entorno (CI/CD vs Producción).

## 🤖 Secretos para CI/CD (GitHub Actions)

El pipeline de integración continua (`maven.yml`) ejecuta pruebas utilizando contenedores Docker efímeros para la base de datos y Redis. La mayoría de configuraciones están "hardcodeadas" en `application-test.properties` para facilitar el testing, pero se requiere un secreto para validar la integridad del build con Google Cloud.

| Secreto | Descripción | Requerido en CI |
|---------|-------------|-----------------|
| `GOOGLE_APPLICATION_CREDENTIALS_JSON` | JSON de cuenta de servicio GCP para validar compilación | ✅ **SÍ** |
| `JWT_SECRET` | Clave de firma de tokens | ❌ No (Usa valor de test) |
| `DB_PASSWORD` | Pass de base de datos | ❌ No (Usa MySQL en Docker) |
| `GOOGLE_MAPS_API_KEY` | Key de Maps | ❌ No (Usa mocks) |

> [!NOTE]
> El entorno de CI levanta automáticamente MySQL y Redis vía Docker. No necesitas configurar credenciales de base de datos en GitHub Secrets para que pasen los tests.

---

## 🚀 Secretos para Producción (Cloud Run / Deploy)

Al desplegar en un entorno real, **SÍ** necesitas configurar todas las siguientes variables de entorno o secretos en tu plataforma de nube (Google Cloud Run revision settings).

### 1. Base de Datos & Caché
- **`DB_NAME`**: Nombre de la base de datos (ej. `messenger_prod`)
- **`DB_USERNAME`**: Usuario de conexión
- **`DB_PASSWORD`**: Contraseña robusta
- **`CLOUD_SQL_CONNECTION_NAME`**: Instancia de Cloud SQL (si usas GCP)
- **`REDIS_HOST`**: Host de tu instancia Redis
- **`REDIS_PASSWORD`**: (Opcional) Auth de Redis

### 2. Seguridad
- **`JWT_SECRET`**:
  - String aleatorio de alta entropía (64+ caracteres).
  - Generar con: `openssl rand -base64 64`
  - **CRÍTICO**: Si este secreto se filtra, todos los tokens pueden ser falsificados.

### 3. Servicios Google
- **`GOOGLE_MAPS_API_KEY`**: Habilitada para Geocoding, Directions y Distance Matrix API.
- **`GCS_BUCKET_NAME`**: Nombre del bucket de Storage para evidencias.
- **`GCP_PROJECT_ID`**: ID de tu proyecto en Google Cloud.

---

## 🛠️ Cómo configurar `GOOGLE_APPLICATION_CREDENTIALS_JSON`

Este es el único secreto complejo requerido para el CI. Sigue estos pasos:

1. Ve a [Google Cloud Console > IAM > Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts).
2. Crea una cuenta de servicio (o usa una existente).
3. Ve a la pestaña **Keys** > **Add Key** > **Create new key** > **JSON**.
4. Se descargará un archivo `.json`.
5. Abre el archivo y copia **todo su contenido**.
6. En GitHub: `Settings` > `Secrets and actions` > `New repository secret`.
7. Nombre: `GOOGLE_APPLICATION_CREDENTIALS_JSON`.
8. Valor: Pega el contenido del JSON.


<details>
<summary><b>Example JSON format</b></summary>

```json
{
  "type": "service_account",
  "project_id": "your-project",
  "private_key_id": "...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "...",
  "client_id": "...",
  "auth_uri": "...",
  "token_uri": "...",
  "auth_provider_x509_cert_url": "...",
  "client_x509_cert_url": "..."
}
```
</details>

---
