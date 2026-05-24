# Guía de Versionamiento - Messenger Backend

Este proyecto utiliza un sistema de versionamiento centralizado para sincronizar la configuración de Maven (`pom.xml`) y la documentación técnica del repositorio.

## Cómo Subir de Versión

Para actualizar la versión del proyecto tienes varias opciones según tu sistema:

- Windows (PowerShell): usa el script PowerShell provisto (`sync-version.ps1`).
- Linux / macOS (Bash): usa el script Bash equivalente (`sync-version.sh`).

### 1) Windows — PowerShell (recomendado si estás en Windows)

Desde una terminal de PowerShell en la raíz del proyecto:
```powershell
.\sync-version.ps1 3.0.0
```

Si recibes un error de Execution Policy:
```powershell
powershell -ExecutionPolicy Bypass -File .\sync-version.ps1 3.0.0
```

### 2) Linux / macOS — Bash

Hemos añadido un script `sync-version.sh` que replica el comportamiento del `.ps1` y es ejecutable en sistemas Unix:
```bash
chmod +x ./sync-version.sh
./sync-version.sh 3.0.0
```

Este script ejecuta `./mvnw versions:set -DnewVersion=<versión> -DgenerateBackupPoms=false` dentro del directorio `messenger` y actualiza los badges `Version-...` en `README.md` y `README.en.md`.

## ¿Qué archivos se actualizan?

1. **pom.xml**: Utiliza el plugin `versions-maven-plugin` para actualizar la versión del proyecto Maven de forma segura.
2. **README.md**: Actualiza automáticamente los badges de versión en la documentación principal.
3. **README.en.md**: Sincroniza la versión en la documentación en inglés.
4. **Swagger/OpenAPI**: Al actualizar el `pom.xml`, la interfaz de documentación técnica (`/swagger-ui/index.html`) reflejará la nueva versión automáticamente en la siguiente compilación.

## Buenas Prácticas (SemVer)

Se recomienda seguir el esquema de **Semantic Versioning**:
- **MAJOR** (X.0.0): Cambios incompatibles en la API o arquitectura.
- **MINOR** (0.X.0): Nuevas funcionalidades compatibles.
- **PATCH** (0.0.X): Corrección de errores y optimizaciones menores.

---

> **Nota**: Después de ejecutar el script, no olvides realizar un commit con los cambios generados:
> `git add .`
> `git commit -m "chore: bump version to 3.0.0"`