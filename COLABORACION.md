# 🚀 Guía de Colaboración

Esta guia es para colaborar en el proyecto Messenger.

## 🛠️ Stack Tecnológico
* **Lenguaje:** Java 17+
* **Framework:** Spring Boot
* **Base de Datos:** MySQL & Redis
* **Infraestructura:** Google Cloud Platform (GCP)

## 🔐 Reglas de Contribución (Seguridad)
Para mantener la integridad del código y cumplir con los estándares del proyecto, el repositorio tiene activadas reglas de protección:

1. **Prohibido Push a `main`:** Nadie puede subir código directamente a la rama principal.
2. **Flujo de Ramas:** Todo cambio debe realizarse en una rama nueva:
   - `git checkout -b feature/nombre-tarea`
3. **Pull Requests (PR):** Para integrar código a `main`, se debe abrir un PR.
4. **Revisión Obligatoria:** Todos los PR requieren la aprobación de **@fttmatteo** (Code Owner) para ser fusionados.
5. **Conversaciones Resueltas:** No se podrá hacer merge si existen comentarios o dudas pendientes en el PR.

## 🚀 Pasos para colaborar
1. Clona el repositorio.
2. Crea tu rama de trabajo.
3. Asegúrate de que el archivo `.env` o credenciales de **MySQL/GCP** estén en el `.gitignore`.
4. Sube tu rama y abre el Pull Request en GitHub.
5. Espera la revisión y aprobación final.