# Migración a Google Cloud Storage

## 📋 Pasos de Configuración

### 1. Configurar Google Cloud Console

1. **Crear proyecto:**
   - Ve a: https://console.cloud.google.com
   - Crea proyecto: `messenger-storage`

2. **Habilitar Cloud Storage API:**
   - Menú → "APIs & Services" → "Enable APIs"
   - Busca "Cloud Storage API" → Enable

3. **Crear bucket:**
   ```
   Nombre: messenger-photos-[tu-nombre-unico]
   Location: us-central1 (o el más cercano)
   Storage class: Standard
   Access control: Uniform
   ```

4. **Configurar acceso público:**
   - En tu bucket → "Permissions"
   - Grant Access → `allUsers` → Role: `Storage Object Viewer`

5. **Crear Service Account:**
   - "IAM & Admin" → "Service Accounts" → "Create"
   - Nombre: `messenger-storage-sa`
   - Role: `Storage Object Admin`
   - Create Key → JSON → **Descargar archivo**

### 2. Configurar Proyecto

1. **Agregar dependencia en `pom.xml`:**
```xml
<dependency>
    <groupId>com.google.cloud</groupId>
    <artifactId>google-cloud-storage</artifactId>
    <version>2.30.1</version>
</dependency>
```

2. **Guardar credenciales:**
   - Coloca el archivo JSON descargado en: `messenger/config/gcs-credentials.json`
   - **IMPORTANTE:** Agrega a `.gitignore`:
     ```
     config/gcs-credentials.json
     ```

3. **Actualizar `application.properties`:**
```properties
# Google Cloud Storage Configuration
google.cloud.storage.bucket-name=messenger-photos-[tu-nombre]
google.cloud.storage.credentials-path=config/gcs-credentials.json
google.cloud.storage.project-id=tu-project-id
```

### 3. Cambiar Implementación

**Opción A: Reemplazar completamente**
- Renombra `FileSystemStorageAdapter` a `FileSystemStorageAdapter.old`
- Renombra `GoogleCloudStorageAdapter` para que Spring lo detecte

**Opción B: Usar perfiles de Spring**
```properties
# application-dev.properties (desarrollo local)
storage.type=filesystem

# application-prod.properties (producción)
storage.type=gcs
```

### 4. Migrar Datos Existentes (Opcional)

Si ya tienes fotos en el sistema de archivos local:

```bash
# Instalar gsutil
curl https://sdk.cloud.google.com | bash

# Autenticarse
gcloud auth login

# Subir archivos existentes
gsutil -m cp -r uploads/* gs://messenger-photos-[tu-nombre]/
```

---

## 💰 Estimación de Costos

### Escenario: Negocio de Mensajería

**Supuestos:**
- 50 mensajeros activos
- 200 entregas/día
- 3 fotos por entrega (600 fotos/día)
- Tamaño promedio: 500 KB/foto
- Retención: 1 año

**Cálculo mensual:**
- Fotos nuevas: 18,000 fotos/mes
- Almacenamiento: 9 GB/mes
- Almacenamiento acumulado año 1: 108 GB

**Costos mensuales (año 1):**
- Almacenamiento: 108 GB × $0.020 = **$2.16 USD**
- Escrituras: 18,000 × ($0.05/10,000) = **$0.09 USD**
- Lecturas (estimado 100,000): 100,000 × ($0.004/10,000) = **$0.04 USD**
- Transferencia (estimado 50 GB): 50 GB × $0.12 = **$6.00 USD**

**Total mensual: ~$8.30 USD** 💵

**Con Free Tier (primeros meses):**
- Primeros 5 GB gratis
- **Total mensual: ~$0.50 USD** 💵

---

## 🔄 Comparación de Costos

### Sistema de Archivos Local
- **Almacenamiento:** Incluido en servidor ($0)
- **Backups:** Debes configurarlos manualmente
- **Escalabilidad:** Limitada por disco del servidor
- **CDN:** Debes configurarlo separadamente
- **Costo total:** $0 + tiempo de mantenimiento

### Google Cloud Storage
- **Almacenamiento:** $0.020/GB/mes
- **Backups:** Automáticos e incluidos
- **Escalabilidad:** Infinita
- **CDN:** Incluido (entrega global rápida)
- **Costo total:** ~$8/mes para 100GB

### Conclusión
GCS es **MÁS BARATO** considerando:
- No necesitas disco adicional en servidor
- No pagas por backups separados
- No pagas por CDN separado
- Reduces costos de servidor (menos almacenamiento)

---

## ✅ Ventajas de GCS

1. **Escalabilidad:** Sube de 100 fotos a 1 millón sin cambios
2. **Rendimiento:** CDN global, entrega rápida en todo el mundo
3. **Confiabilidad:** 99.999999999% durabilidad (11 nueves)
4. **Seguridad:** Encriptación automática en reposo y tránsito
5. **Costo:** Solo pagas por lo que usas
6. **Mantenimiento:** Cero, Google se encarga de todo

---

## 🚀 Próximos Pasos

1. ✅ Crear cuenta en Google Cloud
2. ✅ Configurar bucket y credenciales
3. ✅ Agregar dependencia Maven
4. ✅ Actualizar `application.properties`
5. ✅ Compilar y probar
6. ✅ (Opcional) Migrar fotos existentes

---

## 🆘 Troubleshooting

### Error: "Credentials not found"
- Verifica que `config/gcs-credentials.json` existe
- Verifica que la ruta en `application.properties` es correcta

### Error: "Bucket not found"
- Verifica que el nombre del bucket es correcto
- Verifica que el bucket está en el mismo proyecto

### Error: "Access denied"
- Verifica que el Service Account tiene rol `Storage Object Admin`
- Verifica que las credenciales son del Service Account correcto

### Fotos no se ven
- Verifica que el bucket tiene permisos públicos (`allUsers` → `Storage Object Viewer`)
- Verifica que la URL generada es correcta

---

## 📞 Soporte

- Documentación oficial: https://cloud.google.com/storage/docs
- Pricing calculator: https://cloud.google.com/products/calculator
- Free tier: https://cloud.google.com/free
