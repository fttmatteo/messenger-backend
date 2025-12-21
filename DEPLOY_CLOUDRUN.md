<div align="center">

# 🚀 Deployment Guide - Google Cloud Run

**Messenger Backend** | Spring Boot 4.0 | Cloud Run Serverless  
Documentation updated: December 2025

[🇪🇸 Español](#-tabla-de-contenidos) • [🇺🇸 English](#-table-of-contents)

</div>

---

<details>
<summary><b>🇺🇸 English Version</b> (Click to expand)</summary>

## 📋 Table of Contents

1. [Executive Summary](#executive-summary)
2. [Prerequisites](#prerequisites)
3. [Deployment Architecture](#deployment-architecture)
4. [Initial Configuration](#initial-configuration)
5. [External Services](#external-services)
6. [Secret Management](#secret-management)
7. [Build & Deploy](#build--deploy)
8. [Verification & Monitoring](#verification--monitoring)
9. [Troubleshooting](#troubleshooting)
10. [Cost Optimization](#cost-optimization)

---

## Executive Summary

This guide documents the complete deployment process of the Messenger backend to Google Cloud Run, a serverless platform that:

- ✅ **Auto-scales** from 0 to 10 instances based on demand
- ✅ **Pay-per-use** - Only pay when there's traffic
- ✅ **Automatic SSL/HTTPS** - No certificates needed
- ✅ **Centralized logs** in Google Cloud Logging
- ✅ **High availability** with built-in load balancing

---

## Prerequisites

### Hardware/Software

- **Google Cloud CLI:** v450.0.0 or higher
- **Docker:** (Optional, Cloud Build handles it)
- **Git:** For version control

### Accounts & Access

- ✅ Google Cloud Platform account with billing enabled
- ✅ Aiven account (free MySQL)
- ✅ Redis Cloud account (free Redis)
- ✅ Google Maps API Key
- ✅ Google Cloud Storage Bucket

### Recommended Knowledge

- Familiarity with Spring Boot
- Basic Docker knowledge
- Command line experience
- REST APIs concepts

---

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    GOOGLE CLOUD RUN                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Spring Boot Instances (0-10 autoscaling)        │  │
│  │   - Dynamic port (8080)                           │  │
│  │   - Logs → Cloud Logging                          │  │
│  │   - Secrets → Secret Manager                      │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
        ┌─────────────────┴─────────────────┐
        ↓                                    ↓
┌──────────────────┐              ┌──────────────────┐
│  Aiven MySQL     │              │  Redis Cloud     │
│  (Free Tier)     │              │  (Free 30MB)     │
│  - defaultdb     │              │  - Cache/Session │
└──────────────────┘              └──────────────────┘
        ↓
┌──────────────────┐              ┌──────────────────┐
│  Cloud Storage   │              │  Cloud Vision    │
│  - Photos/Files  │              │  - OCR/Plates    │
└──────────────────┘              └──────────────────┘
```

**Data Flow:**
1. Client → Cloud Run (HTTPS)
2. Cloud Run → Secret Manager (credentials)
3. Spring Boot → Aiven MySQL (persistent data)
4. Spring Boot → Redis Cloud (cache/sessions)
5. Spring Boot → Cloud Storage (files)
6. Spring Boot → Cloud Vision (OCR)

---

## Initial Configuration

### 1. Install Google Cloud CLI

#### macOS
```bash
brew install --cask google-cloud-sdk
```

#### Linux
```bash
curl https://sdk.cloud.google.com | bash
exec -l $SHELL
```

#### Windows (WSL2)
Follow official guide: https://cloud.google.com/sdk/docs/install

### 2. Authentication and Configuration

```bash
# Login to Google Cloud
gcloud auth login

# List existing projects
gcloud projects list

# Create new project (optional)
gcloud projects create messenger-backend-prod \
  --name="Messenger Backend Production"

# Select project
gcloud config set project YOUR_PROJECT_ID

# Set default region
gcloud config set run/region us-central1

# Verify configuration
gcloud config list
```

### 3. Enable Google Cloud APIs

```bash
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  storage.googleapis.com \
  vision.googleapis.com
```

**Estimated time:** 2-3 minutes

### 4. Create Artifact Registry

```bash
gcloud artifacts repositories create messenger-repo \
  --repository-format=docker \
  --location=us-central1 \
  --description="Messenger backend Docker images"

# Verify creation
gcloud artifacts repositories list --location=us-central1
```

---

## External Services

### MySQL Database (Aiven - Free Tier)

**Why Aiven?** Google Cloud SQL costs ~$15 USD/month. Aiven offers free MySQL with 1GB storage.

#### Steps:

1. **Registration**: [aiven.io](https://aiven.io/)
2. **Create service**:
   - Product: **MySQL 8**
   - Cloud Provider: **Google Cloud Platform**
   - Region: **us-east-1** (or nearby)
   - Plan: **Free** ✅
3. **Get credentials**:
   - Service URI: `mysql://avnadmin:PASSWORD@HOST:PORT/defaultdb?ssl-mode=REQUIRED`
   - Host: `mysql-xxxxx.aivencloud.com`
   - Port: `28433` (example)
   - Database: `defaultdb`
   - Username: `avnadmin`
   - Password: (auto-generated)

**Convert to JDBC format:**
```
jdbc:mysql://mysql-xxxxx.aivencloud.com:28433/defaultdb?sslMode=REQUIRED
```

> [!IMPORTANT]
> Save these credentials, you'll need them in the Secrets section.

### Redis Cache (Redis Cloud - Free Tier)

**Why Redis Cloud?** Google Cloud Memorystore costs ~$50 USD/month. Redis Cloud offers 30MB free.

#### Steps:

1. **Registration**: [redis.com/try-free](https://redis.com/try-free/)
2. **Create subscription**:
   - Cloud: **Google Cloud**
   - Tier: **Fixed - Free** (30MB)
3. **Create database**:
   - Database Name: `messenger-cache`
   - Type: **Redis Stack** or **Redis**
4. **Get credentials**:
   - Public Endpoint: `redis-xxxxx.c123.us-central1-1.gce.cloud.redislabs.com:12345`
   - Default User Password: (auto-generated)

**Extract:**
- Host: `redis-xxxxx.c123.us-central1-1.gce.cloud.redislabs.com`
- Port: `12345`
- Password: (the auto-generated one)

### Google Cloud Storage (Bucket for Photos)

```bash
# Create bucket for photos
gsutil mb -l us-central1 gs://messenger-backend-photos

# Configure as public (optional, only if you want public URLs)
gsutil iam ch allUsers:objectViewer gs://messenger-backend-photos
```

### Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. APIs & Services → Credentials
3. Create Credentials → API Key
4. Restrict Key → Maps APIs:
   - ✅ Geocoding API
   - ✅ Directions API
   - ✅ Distance Matrix API
   - ✅ Time Zone API

---

## Secret Management

### Create Secrets in Google Secret Manager

> [!CAUTION]
> **NEVER** commit secrets to the repository. Use environment variables or Secret Manager.

```bash
# === IMPORTANT: Replace with your actual values ===

# 1. JWT Secret (generate a random long one)
echo -n "$(openssl rand -base64 32)" | gcloud secrets create JWT_SECRET --data-file=-

# 2. MySQL Database (Aiven)
echo -n "jdbc:mysql://YOUR_AIVEN_HOST:YOUR_PORT/defaultdb?sslMode=REQUIRED" | \
  gcloud secrets create DB_URL --data-file=-

echo -n "avnadmin" | gcloud secrets create DB_USERNAME --data-file=-
echo -n "YOUR_AIVEN_PASSWORD" | gcloud secrets create DB_PASSWORD --data-file=-

# 3. Redis Cache (Redis Cloud)
echo -n "YOUR_REDIS_HOST" | gcloud secrets create REDIS_HOST --data-file=-
echo -n "YOUR_REDIS_PORT" | gcloud secrets create REDIS_PORT --data-file=-
echo -n "YOUR_REDIS_PASSWORD" | gcloud secrets create REDIS_PASSWORD --data-file=-

# 4. Google APIs
echo -n "YOUR_GOOGLE_MAPS_API_KEY" | gcloud secrets create GOOGLE_MAPS_API_KEY --data-file=-
echo -n "messenger-backend-photos" | gcloud secrets create GCS_BUCKET_NAME --data-file=-
```

### Grant Permissions to Cloud Run

```bash
# Get project number
PROJECT_NUMBER=$(gcloud projects describe $(gcloud config get-value project) \
  --format='value(projectNumber)')

# Grant access to Secret Manager (ALL secrets)
for SECRET in JWT_SECRET DB_URL DB_USERNAME DB_PASSWORD \
              REDIS_HOST REDIS_PORT REDIS_PASSWORD \
              GOOGLE_MAPS_API_KEY GCS_BUCKET_NAME; do
  gcloud secrets add-iam-policy-binding $SECRET \
    --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
done

# Grant access to Cloud Storage
gcloud projects add-iam-policy-binding $(gcloud config get-value project) \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/storage.objectAdmin"

# Grant access to Cloud Vision
gcloud projects add-iam-policy-binding $(gcloud config get-value project) \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/serviceusage.serviceUsageConsumer"
```

---

## Build & Deploy

### 1. Build Docker Image

```bash
cd /path/to/messenger-backend/messenger

# Cloud Build builds automatically
gcloud builds submit \
  --tag us-central1-docker.pkg.dev/YOUR_PROJECT_ID/messenger-repo/messenger-backend:latest
```

**Estimated time:** 2-3 minutes

**What does Cloud Build do?**
1. Uploads source code to Cloud Storage
2. Executes `Dockerfile` multi-stage build
3. Compiles with Maven (Java 21)
4. Creates optimized image with Alpine Linux
5. Uploads image to Artifact Registry

### 2. Deploy to Cloud Run

```bash
gcloud run deploy messenger-backend \
  --image us-central1-docker.pkg.dev/YOUR_PROJECT_ID/messenger-repo/messenger-backend:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 1Gi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --timeout 300 \
  --set-secrets="JWT_SECRET=JWT_SECRET:latest,DB_URL=DB_URL:latest,DB_USERNAME=DB_USERNAME:latest,DB_PASSWORD=DB_PASSWORD:latest,REDIS_HOST=REDIS_HOST:latest,REDIS_PORT=REDIS_PORT:latest,REDIS_PASSWORD=REDIS_PASSWORD:latest,GOOGLE_MAPS_API_KEY=GOOGLE_MAPS_API_KEY:latest,GCS_BUCKET_NAME=GCS_BUCKET_NAME:latest" \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,GCP_PROJECT_ID=YOUR_PROJECT_ID,WEBSOCKET_ALLOWED_ORIGINS=*,CORS_ALLOWED_ORIGINS=*"
```

**Important parameters:**
- `--memory 1Gi`: 1GB of RAM (enough for Spring Boot)
- `--cpu 1`: 1 vCPU
- `--min-instances 0`: Scales to zero when no traffic (saves money)
- `--max-instances 10`: Maximum 10 concurrent instances
- `--timeout 300`: 5-minute timeout (for long requests)
- `--allow-unauthenticated`: Public API (Spring Security handles auth)

> [!WARNING]
> `WEBSOCKET_ALLOWED_ORIGINS=*` and `CORS_ALLOWED_ORIGINS=*` allow any origin. In production, change them to your frontend URL (e.g., `https://app.yourdomain.com`).

**Estimated time:** 1-2 minutes

---

## Verification & Monitoring

### Health Check

```bash
# Get service URL
SERVICE_URL=$(gcloud run services describe messenger-backend \
  --region us-central1 \
  --format='value(status.url)')

echo "Service deployed at: $SERVICE_URL"

# Verify health check
curl $SERVICE_URL/actuator/health

# Expected response:
# {"error": "Unauthorized", "message": "Access denied..."}
# ☝️ This is CORRECT - Spring Security is active
```

### View Logs

```bash
# Real-time logs
gcloud run services logs read messenger-backend \
  --region us-central1 \
  --limit 50 \
  --format "table(timestamp, textPayload)"

# Logs from the last hour
gcloud logging read \
  "resource.type=cloud_run_revision AND resource.labels.service_name=messenger-backend" \
  --limit 100 \
  --format "table(timestamp, severity, textPayload)"
```

### Performance Metrics

```bash
# View metrics from the last hour
gcloud monitoring time-series list \
  --filter 'metric.type="run.googleapis.com/request_count" AND resource.labels.service_name="messenger-backend"' \
  --format json
```

Or access visually:
- **Cloud Console:** [console.cloud.google.com/run](https://console.cloud.google.com/run)
- **Metrics tab** → Request count, Latency, Error rate

---

## Troubleshooting

### Problem 1: Container failed to start

**Error:**
```
The user-provided container failed to start and listen on the port defined by the PORT=8080 environment variable
```

**Common causes:**
1. ❌ Application not listening on `${PORT}`
2. ❌ DB/Redis connection issues
3. ❌ Logging configuration errors

**Solution:**
```bash
# View error logs
gcloud logging read \
  "resource.type=cloud_run_revision AND severity>=ERROR" \
  --limit 20 \
  --format "value(textPayload)"
```

**Verifications:**
- ✅ `application-prod.properties` has `server.port=${PORT:8080}`
- ✅ Secrets configured correctly
- ✅ Logback configured for `CONSOLE` (not files)

### Problem 2: Database connection timeout

**Error:**
```
Could not open JDBC Connection
```

**Solution:**
1. Verify Aiven MySQL is active
2. Verify `DB_URL` format:
   ```
   jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED
   ```
3. Verify credentials in Secret Manager

### Problem 3: Out of memory

**Error:**
```
OutOfMemoryError: Java heap space
```

**Solution:**
```bash
# Increase memory to 2Gi
gcloud run services update messenger-backend \
  --memory 2Gi \
  --region us-central1
```

### Problem 4: Slow cold start

**Symptom:** First request takes 20+ seconds

**Solution:**
```bash
# Keep 1 instance always active (costs ~$7 USD/month)
gcloud run services update messenger-backend \
  --min-instances 1 \
  --region us-central1
```

---

## Cost Optimization

### Cost Estimation (Maximum Free Tier Usage)

| Service | Plan | Monthly Cost |
|---------|------|--------------|
| **Cloud Run** | 0-1 instances, <2M requests | **$0 - $2** |
| **Aiven MySQL** | Free Tier (1GB) | **$0** |
| **Redis Cloud** | Free Tier (30MB) | **$0** |
| **Cloud Storage** | <5GB | **$0.10** |
| **Cloud Build** | <120 builds/month | **$0** |
| **Secret Manager** | <10 secrets | **$0** |
| **Cloud Vision** | <1000 requests/month | **$0** |
| **TOTAL** | | **~$0.10 - $2.10 USD/month** |

### Recommendations to Minimize Costs

1. **Maintain `min-instances=0`** - Scales to zero when no traffic
2. **Use Free Tiers** - Aiven + Redis Cloud are free
3. **Optimize images** - Alpine Linux image (~200MB vs 400MB+)
4. **Aggressive caching** - Redis reduces MySQL queries
5. **Compress responses** - `server.compression.enabled=true`

---

## Current Project Configuration

### Database Migration Status

**Configuration:** `spring.jpa.hibernate.ddl-auto=validate`

- ✅ **Hibernate in validation mode** - Only validates schema, doesn't modify
- ✅ **Existing tables** - Previously created
- ⚠️ **Flyway not auto-executed** - Configured but not active in Spring Boot 4.0

**For future migrations:**
1. Run SQL scripts manually in Aiven
2. Update JPA entities
3. Rebuild and redeploy

### Logging

**Configuration:** Logs to console (stdout)

- ✅ Cloud Run captures stdout → Cloud Logging
- ✅ Read-only file system compatible
- ✅ Structured logs with Logback

**Log levels in production:**
- `root=WARN`
- `app=INFO`
- `org.springframework=WARN`
- `org.flywaydb=DEBUG`

---

## Quick Reference Commands

```bash
# View service status
gcloud run services describe messenger-backend --region us-central1

# View revisions
gcloud run revisions list --service messenger-backend --region us-central1

# Rollback to previous revision
gcloud run services update-traffic messenger-backend \
  --to-revisions REVISION_NAME=100 \
  --region us-central1

# View secrets
gcloud secrets list

# Update secret
echo -n "NEW_VALUE" | gcloud secrets versions add SECRET_NAME --data-file=-

# View real-time logs
gcloud run services logs tail messenger-backend --region us-central1

# Delete old revision
gcloud run revisions delete REVISION_NAME --region us-central1

# Delete entire service
gcloud run services delete messenger-backend --region us-central1
```

---

## CI/CD (Optional)

### GitHub Actions (Automatic Deployment)

Create `.github/workflows/deploy-cloudrun.yml`:

```yaml
name: Deploy to Cloud Run

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Authenticate to Google Cloud
        uses: google-github-actions/auth@v1
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}
      
      - name: Build and Deploy
        run: |
          gcloud builds submit \
            --tag us-central1-docker.pkg.dev/$PROJECT_ID/messenger-repo/messenger-backend:latest
          
          gcloud run deploy messenger-backend \
            --image us-central1-docker.pkg.dev/$PROJECT_ID/messenger-repo/messenger-backend:latest \
            --region us-central1
```

---

## Support & Contact

**Official Documentation:**
- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Spring Boot on Cloud Run](https://cloud.google.com/run/docs/quickstarts/build-and-deploy/deploy-java-service)

**Project Specific:**
- Repository: `messenger-backend`
- Email: valenciaardila988@icloud.com
- Author: Matteo
- Last update: December 2025

</details>

---

# Guía Profesional de Despliegue - Google Cloud Run

**Messenger Backend** | Spring Boot 4.0 | Cloud Run Serverless  
Documentación actualizada: Diciembre 2025

---

## 📋 Tabla de Contenidos

1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Requisitos Previos](#requisitos-previos)
3. [Arquitectura de Despliegue](#arquitectura-de-despliegue)
4. [Configuración Inicial](#configuración-inicial)
5. [Servicios Externos](#servicios-externos)
6. [Gestión de Secretos](#gestión-de-secretos)
7. [Build y Despliegue](#build-y-despliegue)
8. [Verificación y Monitoreo](#verificación-y-monitoreo)
9. [Troubleshooting](#troubleshooting)
10. [Optimización de Costos](#optimización-de-costos)

---

## Resumen Ejecutivo

Esta guía documenta el proceso completo de despliegue del backend Messenger en Google Cloud Run, una plataforma serverless que:

- ✅ **Escala automáticamente** de 0 a 10 instancias según demanda
- ✅ **Pago por uso** - Solo pagas cuando hay tráfico
- ✅ **SSL/HTTPS automático** - Sin necesidad de certificados
- ✅ **Logs centralizados** en Google Cloud Logging
- ✅ **Alta disponibilidad** con balanceo de carga incluido

---

## Requisitos Previos

### Hardware/Software

- **Google Cloud CLI:** v450.0.0 o superior
- **Docker:** (Opcional, Cloud Build lo maneja)
- **Git:** Para control de versiones

### Cuentas & Accesos

- ✅ Cuenta de Google Cloud Platform con facturación habilitada
- ✅ Cuenta Aiven (MySQL gratuito)
- ✅ Cuenta Redis Cloud (Redis gratuito)
- ✅ Google Maps API Key
- ✅ Google Cloud Storage Bucket

### Conocimientos Recomendados

- Familiaridad con Spring Boot
- Conocimientos básicos de Docker
- Experiencia con línea de comandos
- Conceptos de REST APIs

---

## Arquitectura de Despliegue

```
┌─────────────────────────────────────────────────────────┐
│                    GOOGLE CLOUD RUN                      │
│  ┌───────────────────────────────────────────────────┐  │
│  │   Instancias Spring Boot (0-10 autoscaling)       │  │
│  │   - Puerto dinámico (8080)                        │  │
│  │   - Logs → Cloud Logging                          │  │
│  │   - Secretos → Secret Manager                     │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                          ↓
        ┌─────────────────┴─────────────────┐
        ↓                                    ↓
┌──────────────────┐              ┌──────────────────┐
│  Aiven MySQL     │              │  Redis Cloud     │
│  (Free Tier)     │              │  (Free 30MB)     │
│  - defaultdb     │              │  - Cache/Session │
└──────────────────┘              └──────────────────┘
        ↓
┌──────────────────┐              ┌──────────────────┐
│  Cloud Storage   │              │  Cloud Vision    │
│  - Photos/Files  │              │  - OCR/Plates    │
└──────────────────┘              └──────────────────┘
```

**Flujo de Datos:**
1. Cliente → Cloud Run (HTTPS)
2. Cloud Run → Secret Manager (credenciales)
3. Spring Boot → Aiven MySQL (datos persistentes)
4. Spring Boot → Redis Cloud (caché/sesiones)
5. Spring Boot → Cloud Storage (archivos)
6. Spring Boot → Cloud Vision (OCR)

---

## Configuración Inicial

### 1. Instalar Google Cloud CLI

#### macOS
```bash
brew install --cask google-cloud-sdk
```

#### Linux
```bash
curl https://sdk.cloud.google.com | bash
exec -l $SHELL
```

#### Windows (WSL2)
Seguir guía oficial: https://cloud.google.com/sdk/docs/install

### 2. Autenticación y Configuración

```bash
# Iniciar sesión en Google Cloud
gcloud auth login

# Listar proyectos existentes
gcloud projects list

# Crear nuevo proyecto (opcional)
gcloud projects create messenger-backend-prod \
  --name="Messenger Backend Production"

# Seleccionar proyecto
gcloud config set project TU_PROJECT_ID

# Configurar región por defecto
gcloud config set run/region us-central1

# Verificar configuración
gcloud config list
```

### 3. Habilitar APIs de Google Cloud

```bash
gcloud services enable \
  run.googleapis.com \
  cloudbuild.googleapis.com \
  artifactregistry.googleapis.com \
  secretmanager.googleapis.com \
  storage.googleapis.com \
  vision.googleapis.com
```

**Tiempo estimado:** 2-3 minutos

### 4. Crear Artifact Registry

```bash
gcloud artifacts repositories create messenger-repo \
  --repository-format=docker \
  --location=us-central1 \
  --description="Imágenes Docker del backend Messenger"

# Verificar creación
gcloud artifacts repositories list --location=us-central1
```

---

## Servicios Externos

### MySQL Database (Aiven - Free Tier)

**¿Por qué Aiven?** Google Cloud SQL cuesta ~$15 USD/mes. Aiven ofrece MySQL gratis con 1GB de almacenamiento.

#### Pasos:

1. **Registro**: [aiven.io](https://aiven.io/)
2. **Crear servicio**:
   - Producto: **MySQL 8**
   - Cloud Provider: **Google Cloud Platform**
   - Región: **us-east-1** (o cercana)
   - Plan: **Free** ✅
3. **Obtener credenciales**:
   - Service URI: `mysql://avnadmin:PASSWORD@HOST:PORT/defaultdb?ssl-mode=REQUIRED`
   - Host: `mysql-xxxxx.aivencloud.com`
   - Port: `28433` (ejemplo)
   - Database: `defaultdb`
   - Username: `avnadmin`
   - Password: (auto-generado)

**Convertir a formato JDBC:**
```
jdbc:mysql://mysql-xxxxx.aivencloud.com:28433/defaultdb?sslMode=REQUIRED
```

> [!IMPORTANT]
> Guarda estas credenciales, las necesitarás en la sección de Secretos.

### Redis Cache (Redis Cloud - Free Tier)

**¿Por qué Redis Cloud?** Google Cloud Memorystore cuesta ~$50 USD/mes. Redis Cloud ofrece 30MB gratis.

#### Pasos:

1. **Registro**: [redis.com/try-free](https://redis.com/try-free/)
2. **Crear suscripción**:
   - Cloud: **Google Cloud**
   - Tier: **Fixed - Free** (30MB)
3. **Crear database**:
   - Database Name: `messenger-cache`
   - Type: **Redis Stack** o **Redis**
4. **Obtener credenciales**:
   - Public Endpoint: `redis-xxxxx.c123.us-central1-1.gce.cloud.redislabs.com:12345`
   - Default User Password: (auto-generado)

**Extraer:**
- Host: `redis-xxxxx.c123.us-central1-1.gce.cloud.redislabs.com`
- Port: `12345`
- Password: (el auto-generado)

### Google Cloud Storage (Bucket para Fotos)

```bash
# Crear bucket público para fotos
gsutil mb -l us-central1 gs://messenger-backend-photos

# Configurar como público (opcional, solo si quieres URLs públicas)
gsutil iam ch allUsers:objectViewer gs://messenger-backend-photos
```

### Google Maps API Key

1. Ve a [Google Cloud Console](https://console.cloud.google.com)
2. APIs & Services → Credentials
3. Create Credentials → API Key
4. Restrict Key → Maps APIs:
   - ✅ Geocoding API
   - ✅ Directions API
   - ✅ Distance Matrix API
   - ✅ Time Zone API

---

## Gestión de Secretos

### Crear Secretos en Google Secret Manager

> [!CAUTION]
> **NUNCA** comitees secretos al repositorio. Usa variables de entorno o Secret Manager.

```bash
# === IMPORTANTE: Reemplaza con tus valores reales ===

# 1. JWT Secret (genera uno aleatorio largo)
echo -n "$(openssl rand -base64 32)" | gcloud secrets create JWT_SECRET --data-file=-

# 2. Base de Datos MySQL (Aiven)
echo -n "jdbc:mysql://TU_HOST_AIVEN:TU_PUERTO/defaultdb?sslMode=REQUIRED" | \
  gcloud secrets create DB_URL --data-file=-

echo -n "avnadmin" | gcloud secrets create DB_USERNAME --data-file=-
echo -n "TU_PASSWORD_AIVEN" | gcloud secrets create DB_PASSWORD --data-file=-

# 3. Redis Cache (Redis Cloud)
echo -n "TU_HOST_REDIS" | gcloud secrets create REDIS_HOST --data-file=-
echo -n "TU_PUERTO_REDIS" | gcloud secrets create REDIS_PORT --data-file=-
echo -n "TU_PASSWORD_REDIS" | gcloud secrets create REDIS_PASSWORD --data-file=-

# 4. Google APIs
echo -n "TU_GOOGLE_MAPS_API_KEY" | gcloud secrets create GOOGLE_MAPS_API_KEY --data-file=-
echo -n "messenger-backend-photos" | gcloud secrets create GCS_BUCKET_NAME --data-file=-
```

### Otorgar Permisos a Cloud Run

```bash
# Obtener número de proyecto
PROJECT_NUMBER=$(gcloud projects describe $(gcloud config get-value project) \
  --format='value(projectNumber)')

# Dar acceso a Secret Manager (TODOS los secretos)
for SECRET in JWT_SECRET DB_URL DB_USERNAME DB_PASSWORD \
              REDIS_HOST REDIS_PORT REDIS_PASSWORD \
              GOOGLE_MAPS_API_KEY GCS_BUCKET_NAME; do
  gcloud secrets add-iam-policy-binding $SECRET \
    --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
    --role="roles/secretmanager.secretAccessor"
done

# Dar acceso a Cloud Storage
gcloud projects add-iam-policy-binding $(gcloud config get-value project) \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/storage.objectAdmin"

# Dar acceso a Cloud Vision
gcloud projects add-iam-policy-binding $(gcloud config get-value project) \
  --member="serviceAccount:$PROJECT_NUMBER-compute@developer.gserviceaccount.com" \
  --role="roles/serviceusage.serviceUsageConsumer"
```

---

## Build y Despliegue

### 1. Construir Imagen Docker

```bash
cd /Users/Matteo/Desktop/messenger-backend/messenger

# Cloud Build construye automáticamente
gcloud builds submit \
  --tag us-central1-docker.pkg.dev/TU_PROJECT_ID/messenger-repo/messenger-backend:latest
```

**Tiempo estimado:** 2-3 minutos

**¿Qué hace Cloud Build?**
1. Sube código fuente a Cloud Storage
2. Ejecuta `Dockerfile` multi-stage build
3. Compila con Maven (Java 21)
4. Crea imagen optimizada con Alpine Linux
5. Sube imagen a Artifact Registry

### 2. Desplegar en Cloud Run

```bash
gcloud run deploy messenger-backend \
  --image us-central1-docker.pkg.dev/TU_PROJECT_ID/messenger-repo/messenger-backend:latest \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --memory 1Gi \
  --cpu 1 \
  --min-instances 0 \
  --max-instances 10 \
  --timeout 300 \
  --set-secrets="JWT_SECRET=JWT_SECRET:latest,DB_URL=DB_URL:latest,DB_USERNAME=DB_USERNAME:latest,DB_PASSWORD=DB_PASSWORD:latest,REDIS_HOST=REDIS_HOST:latest,REDIS_PORT=REDIS_PORT:latest,REDIS_PASSWORD=REDIS_PASSWORD:latest,GOOGLE_MAPS_API_KEY=GOOGLE_MAPS_API_KEY:latest,GCS_BUCKET_NAME=GCS_BUCKET_NAME:latest" \
  --set-env-vars="SPRING_PROFILES_ACTIVE=prod,GCP_PROJECT_ID=TU_PROJECT_ID,WEBSOCKET_ALLOWED_ORIGINS=*,CORS_ALLOWED_ORIGINS=*"
```

**Parámetros importantes:**
- `--memory 1Gi`: 1GB de RAM (suficiente para Spring Boot)
- `--cpu 1`: 1 vCPU
- `--min-instances 0`: Escala a cero cuando no hay tráfico (ahorra dinero)
- `--max-instances 10`: Máximo 10 instancias concurrentes
- `--timeout 300`: Timeout de 5 minutos (para requests largos)
- `--allow-unauthenticated`: API pública (Spring Security maneja auth)

> [!WARNING]
> `WEBSOCKET_ALLOWED_ORIGINS=*` y `CORS_ALLOWED_ORIGINS=*` permiten cualquier origen. En producción, cámbialos por la URL de tu frontend (ej: `https://app.tudominio.com`).

**Tiempo estimado:** 1-2 minutos

---

## Verificación y Monitoreo

### Health Check

```bash
# Obtener URL del servicio
SERVICE_URL=$(gcloud run services describe messenger-backend \
  --region us-central1 \
  --format='value(status.url)')

echo "Servicio desplegado en: $SERVICE_URL"

# Verificar health check
curl $SERVICE_URL/actuator/health

# Respuesta esperada:
# {"error": "Unauthorized", "message": "Acceso denegado..."}
# ☝️ Esto es CORRECTO - Spring Security está activo
```

### Ver Logs

```bash
# Logs en tiempo real
gcloud run services logs read messenger-backend \
  --region us-central1 \
  --limit 50 \
  --format "table(timestamp, textPayload)"

# Logs de última hora
gcloud logging read \
  "resource.type=cloud_run_revision AND resource.labels.service_name=messenger-backend" \
  --limit 100 \
  --format "table(timestamp, severity, textPayload)"
```

### Métricas de Rendimiento

```bash
# Ver métricas de la última hora
gcloud monitoring time-series list \
  --filter 'metric.type="run.googleapis.com/request_count" AND resource.labels.service_name="messenger-backend"' \
  --format json
```

O accede visualmente a:
- **Cloud Console:** [console.cloud.google.com/run](https://console.cloud.google.com/run)
- **Métricas tab** → Request count, Latency, Error rate

---

## Troubleshooting

### Problema 1: Container failed to start

**Error:**
```
The user-provided container failed to start and listen on the port defined by the PORT=8080 environment variable
```

**Causas comunes:**
1. ❌ Aplicación no escucha en `${PORT}`
2. ❌ Problemas de conexión a DB/Redis
3. ❌ Errores en configuración de logging

**Solución:**
```bash
# Ver logs de error
gcloud logging read \
  "resource.type=cloud_run_revision AND severity>=ERROR" \
  --limit 20 \
  --format "value(textPayload)"
```

**Verificaciones:**
- ✅ `application-prod.properties` tiene `server.port=${PORT:8080}`
- ✅ Secretos configurados correctamente
- ✅ Logback configurado para `CONSOLE` (no archivos)

### Problema 2: Database connection timeout

**Error:**
```
Could not open JDBC Connection
```

**Solución:**
1. Verificar que Aiven MySQL esté activo
2. Verificar formato de `DB_URL`:
   ```
   jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED
   ```
3. Verificar credenciales en Secret Manager

### Problema 3: Out of memory

**Error:**
```
OutOfMemoryError: Java heap space
```

**Solución:**
```bash
# Aumentar memoria a 2Gi
gcloud run services update messenger-backend \
  --memory 2Gi \
  --region us-central1
```

### Problema 4: Cold start lento

**Síntoma:** Primera request tarda 20+ segundos

**Solución:**
```bash
# Mantener 1 instancia siempre activa (cuesta ~$7 USD/mes)
gcloud run services update messenger-backend \
  --min-instances 1 \
  --region us-central1
```

---

## Optimización de Costos

### Estimación de Costos (Free Tier Máximo)

| Servicio | Plan | Costo Mensual |
|----------|------|---------------|
| **Cloud Run** | 0-1 instancias, <2M requests | **$0 - $2** |
| **Aiven MySQL** | Free Tier (1GB) | **$0** |
| **Redis Cloud** | Free Tier (30MB) | **$0** |
| **Cloud Storage** | <5GB | **$0.10** |
| **Cloud Build** | <120 builds/mes | **$0** |
| **Secret Manager** | <10 secretos | **$0** |
| **Cloud Vision** | <1000 requests/mes | **$0** |
| **TOTAL** | | **~$0.10 - $2.10 USD/mes** |

### Recomendaciones para Minimizar Costos

1. **Mantener `min-instances=0`** - Escala a cero cuando no hay tráfico
2. **Usar Free Tiers** - Aiven + Redis Cloud son gratis
3. **Optimizar imágenes** - Imagen Alpine Linux (~200MB vs 400MB+)
4. **Caché agresivo** - Redis reduce queries a MySQL
5. **Comprimir responses** - `server.compression.enabled=true`

---

## Configuración Actual del Proyecto

### Estado de Migraciones de Base de Datos

**Configuración:** `spring.jpa.hibernate.ddl-auto=validate`

- ✅ **Hibernate en modo validación** - Solo valida esquema, no modifica
- ✅ **Tablas existentes** - Creadas previamente
- ⚠️ **Flyway no auto-ejecutado** - Configurado pero no activo en Spring Boot 4.0

**Para futuras migraciones:**
1. Ejecutar scripts SQL manualmente en Aiven
2. Actualizar entidades JPA
3. Rebuild y redeploy

### Logging

**Configuración:** Logs a consola (stdout)

- ✅ Cloud Run captura stdout → Cloud Logging
- ✅ Sistema de archivos read-only compatible
- ✅ Logs estructurados con Logback

**Niveles de log en producción:**
- `root=WARN`
- `app=INFO`
- `org.springframework=WARN`
- `org.flywaydb=DEBUG`

---

## Comandos de Referencia Rápida

```bash
# Ver estado del servicio
gcloud run services describe messenger-backend --region us-central1

# Ver revisiones
gcloud run revisions list --service messenger-backend --region us-central1

# Rollback a revisión anterior
gcloud run services update-traffic messenger-backend \
  --to-revisions REVISION_NAME=100 \
  --region us-central1

# Ver secretos
gcloud secrets list

# Actualizar secreto
echo -n "NUEVO_VALOR" | gcloud secrets versions add NOMBRE_SECRETO --data-file=-

# Ver logs en tiempo real
gcloud run services logs tail messenger-backend --region us-central1

# Eliminar revisión antigua
gcloud run revisions delete REVISION_NAME --region us-central1

# Eliminar servicio completo
gcloud run services delete messenger-backend --region us-central1
```

---

## CI/CD (Opcional)

### GitHub Actions (Despliegue Automático)

Crear `.github/workflows/deploy-cloudrun.yml`:

```yaml
name: Deploy to Cloud Run

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Authenticate to Google Cloud
        uses: google-github-actions/auth@v1
        with:
          credentials_json: ${{ secrets.GCP_SA_KEY }}
      
      - name: Build and Deploy
        run: |
          gcloud builds submit \
            --tag us-central1-docker.pkg.dev/$PROJECT_ID/messenger-repo/messenger-backend:latest
          
          gcloud run deploy messenger-backend \
            --image us-central1-docker.pkg.dev/$PROJECT_ID/messenger-repo/messenger-backend:latest \
            --region us-central1
```

---

## Soporte y Contacto

**Documentación Oficial:**
- [Cloud Run Documentation](https://cloud.google.com/run/docs)
- [Spring Boot on Cloud Run](https://cloud.google.com/run/docs/quickstarts/build-and-deploy/deploy-java-service)

**Proyecto Específico:**
- Repository: `messenger-backend`
- Autor: Matteo
- Email: valenciaardila988@icloud.com
- Última actualización: Diciembre 2025

---
