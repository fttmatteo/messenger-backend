# Guía de Versionamiento - Messenger Backend

Este proyecto utiliza un sistema de versionamiento centralizado para sincronizar la configuración de Maven (`pom.xml`) y la documentación técnica del repositorio.

## Cómo Subir de Versión

Para actualizar la versión del proyecto, utiliza el script de PowerShell proporcionado en la raíz del repositorio.

### Comando Estándar

Desde una terminal de PowerShell en la raíz del proyecto:
```powershell
.\sync-version.ps1 1.11.8
```

### En caso de Restricciones de Política (Execution Policy)

Si recibes un error de "UnauthorizedAccess" o políticas de ejecución, utiliza:
```powershell
powershell -ExecutionPolicy Bypass -File .\sync-version.ps1 1.11.8
```

## ¿Qué archivos se actualizan?

1. **pom.xml**: Utiliza el plugin `versions-maven-plugin` para actualizar la versión del proyecto Maven de forma segura.
2. **README.md**: Actualiza automáticamente los badges de versión en la documentación principal.
3. **README.en.md**: Sincroniza la versión en la documentación en inglés.
4. **Swagger/OpenAPI**: Al actualizar el `pom.xml`, la interfaz de documentación técnica (`/swagger-ui/index.html`) reflejará la nueva versión automáticamente en la siguiente compilación.

## Recomendaciones (SemVer)

- **PATCH**: Para correcciones de errores, refactorizaciones y optimizaciones de rendimiento (como las realizadas en la sesión actual).
- **MINOR**: Para nuevos endpoints o funcionalidades de negocio.
- **MAJOR**: Para cambios estructurales que rompan la compatibilidad con clientes antiguos.

---

> **Nota**: Este script debe ejecutarse desde la raíz del repositorio `messenger-backend`.
