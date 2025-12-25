# GitHub Actions - Secrets Requeridos

Para que el workflow de CI/CD funcione correctamente, necesitas configurar los siguientes **secrets** en tu repositorio de GitHub.

## Cómo configurar secrets en GitHub

1. Ve a tu repositorio en GitHub
2. Click en **Settings** → **Secrets and variables** → **Actions**
3. Click en **New repository secret**
4. Agrega cada uno de los secrets listados abajo

---

## 🔐 Secrets Requeridos

### 1. JWT_SECRET
- **Descripción**: Clave secreta para firmar y verificar tokens JWT
- **Tipo**: String aleatorio seguro
- **Ejemplo de generación**:
  ```bash
  openssl rand -base64 64
  ```
- **CRÍTICO**: Debe ser el mismo valor que usas en producción

---

### 2. GOOGLE_MAPS_API_KEY
- **Descripción**: API key de Google Maps para servicios de geocodificación
- **Dónde obtenerla**: [Google Cloud Console](https://console.cloud.google.com/apis/credentials)
- **Tipo**: String
- **Formato**: `AIza...`
- **Nota**: Puede ser una key de testing para el CI

---

### 3. GCS_BUCKET_NAME
- **Descripción**: Nombre del bucket de Google Cloud Storage
- **Tipo**: String
- **Ejemplo**: `messenger-backend-storage` o `messenger-backend-test`
- **Nota**: Puede ser un bucket de testing/desarrollo

---

### 4. GCP_PROJECT_ID
- **Descripción**: ID del proyecto de Google Cloud Platform
- **Dónde obtenerlo**: [Google Cloud Console](https://console.cloud.google.com/)
- **Tipo**: String
- **Formato**: `my-project-id-12345`

---

### 5. GOOGLE_APPLICATION_CREDENTIALS_JSON
- **Descripción**: Contenido completo del archivo JSON de credenciales de servicio de GCP
- **Tipo**: JSON string
- **Cómo obtenerlo**:
  1. Ve a [Google Cloud Console](https://console.cloud.google.com/iam-admin/serviceaccounts)
  2. Crea una cuenta de servicio (o usa una existente)
  3. Click en la cuenta → **Keys** → **Add Key** → **Create new key** → **JSON**
  4. Descarga el archivo JSON
  5. Copia **todo el contenido del archivo** (incluyendo las llaves `{}`)
- **Permisos necesarios** para la cuenta de servicio:
  - `Cloud Storage Object Admin` (para GCS)
  - `Cloud Vision API User` (para OCR)
  
**Ejemplo del formato del JSON**:
```json
{
  "type": "service_account",
  "project_id": "tu-proyecto",
  "private_key_id": "abc123...",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "mi-servicio@tu-proyecto.iam.gserviceaccount.com",
  "client_id": "123456789",
  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
  "token_uri": "https://oauth2.googleapis.com/token",
  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/..."
}
```

---

## ✅ Verificación

Una vez configurados todos los secrets:

1. **Verifica en GitHub**: Settings → Secrets and variables → Actions
   - Deberías ver los 5 secrets listados
   
2. **Prueba el workflow**:
   - Crea un pull request o haz push a `main` o `develop`
   - El workflow debería ejecutarse automáticamente
   - Revisa los logs en la pestaña **Actions** del repositorio

---

## 📝 Notas Importantes

- **NO uses credenciales de producción** para CI/CD si es posible
- Crea una cuenta de servicio y bucket específicos para testing
- Los secrets son **variables de entorno** encriptadas que GitHub inyecta durante la ejecución
- Nunca commitees secrets en el código
- H2 se usa automáticamente para tests (base de datos en memoria)

---

## 🔄 Dependabot

Ya está configurado en `.github/dependabot.yml` para:
- Revisar actualizaciones **diariamente a las 8:00 AM (hora de Bogotá)**
- Crear PRs automáticos para:
  - Dependencias Maven
  - GitHub Actions
- Agrupar actualizaciones relacionadas (Spring, Google Cloud, Security)
