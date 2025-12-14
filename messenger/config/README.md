# ============================================
# CONFIGURATION TEMPLATES README
# ============================================

Este directorio contiene archivos de configuración sensibles para el proyecto.

## 📁 Estructura de Archivos

```
config/
├── secrets.properties                      ✅ SECRETO (NO en Git)
├── secrets.properties.template             📄 Template (en Git)
├── google-maps-config.properties           ✅ SECRETO (NO en Git)
├── google-maps-config.properties.template  📄 Template (en Git)
├── google-vision-credentials.json          ✅ SECRETO (NO en Git)
├── google-vision-credentials.json.template 📄 Template (en Git)
├── messenger-storage.json                  ✅ SECRETO (NO en Git)
├── messenger-storage.json.template         📄 Template (en Git)
└── README.md                               📄 Este archivo (en Git)
```

## 🔒 Archivos Secretos (NO hacer commit)

Estos archivos contienen credenciales reales y están protegidos en `.gitignore`:

1. **secrets.properties**
   - JWT Secret Key
   - Generado con: `openssl rand -base64 64`

2. **google-maps-config.properties**
   - API Key de Google Maps Platform
   - Obtener en: https://console.cloud.google.com/apis/credentials

3. **google-vision-credentials.json**
   - Service Account para Cloud Vision API
   - Descargado desde Google Cloud Console

4. **messenger-storage.json**
   - Service Account para Cloud Storage
   - Descargado desde Google Cloud Console

## 📄 Archivos Template (SÍ hacer commit)

Estos archivos son templates de referencia con instrucciones:

- `*.template` - Estructura y ejemplos
- Incluyen instrucciones de cómo obtener credenciales
- **NO** contienen credenciales reales

## 🚀 Setup Inicial

### Para un nuevo desarrollador:

1. **Clona el repositorio**
   ```bash
   git clone <repo-url>
   cd messenger-backend/messenger
   ```

2. **Copia los templates**
   ```bash
   cd config
   cp secrets.properties.template secrets.properties
   cp google-maps-config.properties.template google-maps-config.properties
   cp google-vision-credentials.json.template google-vision-credentials.json
   cp messenger-storage.json.template messenger-storage.json
   ```

3. **Solicita credenciales**
   - Pide al líder del equipo las credenciales de desarrollo
   - O crea tus propias credenciales siguiendo las instrucciones en cada template

4. **Completa cada archivo**
   - Edita cada archivo copiado
   - Reemplaza los placeholders con credenciales reales
   - Sigue las instrucciones en cada template

5. **Verifica**
   ```bash
   git status
   # Los archivos .template deben aparecer
   # Los archivos sin .template NO deben aparecer
   ```

## 🔐 Seguridad

### ✅ Hacer:
- Guardar credenciales de forma segura
- Usar credenciales diferentes por entorno (dev, staging, prod)
- Rotar credenciales periódicamente
- Configurar permisos mínimos necesarios

### ❌ NO Hacer:
- Hacer commit de archivos con credenciales reales
- Compartir credenciales por email o chat
- Usar las mismas credenciales en todos los entornos
- Subir credenciales a servicios públicos (Pastebin, Gist, etc.)

## 📋 Checklist de Configuración

- [ ] `secrets.properties` creado y configurado
- [ ] `google-maps-config.properties` creado y configurado
- [ ] `google-vision-credentials.json` creado y configurado
- [ ] `messenger-storage.json` creado y configurado
- [ ] Todos los archivos secretos están en `.gitignore`
- [ ] `git status` no muestra archivos secretos
- [ ] La aplicación inicia correctamente
- [ ] Los tests pasan

## 🆘 Troubleshooting

### "Could not resolve placeholder"
- Verifica que el archivo de configuración existe
- Verifica que el archivo tiene el formato correcto
- Verifica que no hay espacios extras

### "Credentials not found"
- Verifica la ruta en `application.properties`
- Verifica permisos del archivo (debe ser legible)
- Verifica que el archivo JSON es válido

### Git muestra archivos secretos
- Verifica `.gitignore`
- Ejecuta: `git rm --cached config/archivo-secreto`
- Asegúrate de que el archivo esté listado en `.gitignore`

## 📞 Contacto

Si tienes problemas:
1. Revisa este README
2. Revisa las instrucciones en cada template
3. Contacta al líder del equipo
