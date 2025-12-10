# Instrucciones para Ejecutar las Migraciones de Base de Datos

## Migración: Google Maps Integration

**Fecha:** 2025-12-10  
**Archivo:** `V2__add_google_maps_integration.sql`

### Descripción

Este script de migración añade soporte completo para Google Maps Platform y tracking en tiempo real:

1. **Tabla `dealerships`**: Añade columnas para almacenar coordenadas geocodificadas
2. **Tabla `status_history`**: Añade columnas para registrar la ubicación de entrega
3. **Tabla `tracking_history`**: Nueva tabla para almacenar historial de ubicaciones de mensajeros

### Cambios en la Base de Datos

#### 1. Dealerships (Concesionarios)
```sql
- latitude (DOUBLE): Latitud del concesionario
- longitude (DOUBLE): Longitud del concesionario
- is_geolocated (BOOLEAN): Indica si el concesionario ha sido geocodificado
```

#### 2. Status History (Historial de Estados)
```sql
- delivery_latitude (DOUBLE): Latitud donde se realizó el cambio de estado
- delivery_longitude (DOUBLE): Longitud donde se realizó el cambio de estado
```

#### 3. Tracking History (Nueva Tabla)
```sql
- history_id (BIGINT): ID único del registro
- messenger_id (BIGINT): ID del mensajero
- latitude (DOUBLE): Latitud de la ubicación
- longitude (DOUBLE): Longitud de la ubicación
- recorded_at (DATETIME): Fecha y hora del registro
- service_delivery_id (BIGINT): Servicio activo (nullable)
- source (ENUM): Fuente de la ubicación (GPS, NETWORK, MANUAL)
- speed (DOUBLE): Velocidad en km/h (nullable)
```

### Cómo Ejecutar la Migración

#### Opción 1: Manualmente con MySQL Client

```bash
# Conectar a la base de datos
mysql -u root -p messenger_db

# Ejecutar el script
source /Users/Matteo/Desktop/messenger-backend/messenger/src/main/resources/db/migration/V2__add_google_maps_integration.sql

# Verificar que las columnas se crearon correctamente
DESCRIBE dealerships;
DESCRIBE status_history;
DESCRIBE tracking_history;
```

#### Opción 2: Con Flyway (Recomendado para Producción)

Si decides usar Flyway para gestión de migraciones:

1. Añade la dependencia en `pom.xml`:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

2. Configura Flyway en `application.properties`:
```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
```

3. Al iniciar la aplicación, Flyway ejecutará automáticamente las migraciones pendientes.

#### Opción 3: Con MySQL Workbench

1. Abre MySQL Workbench
2. Conecta a tu base de datos
3. Abre el archivo `V2__add_google_maps_integration.sql`
4. Ejecuta el script completo

### Verificación Post-Migración

Ejecuta estas queries para verificar que todo se creó correctamente:

```sql
-- Verificar columnas en dealerships
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'dealerships' 
  AND COLUMN_NAME IN ('latitude', 'longitude', 'is_geolocated');

-- Verificar columnas en status_history
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'status_history' 
  AND COLUMN_NAME IN ('delivery_latitude', 'delivery_longitude');

-- Verificar que la tabla tracking_history existe
SHOW TABLES LIKE 'tracking_history';

-- Ver estructura completa de tracking_history
DESCRIBE tracking_history;
```

### Rollback (En caso de error)

Si necesitas revertir los cambios:

```sql
-- Eliminar columnas de dealerships
ALTER TABLE dealerships 
DROP COLUMN latitude,
DROP COLUMN longitude,
DROP COLUMN is_geolocated;

-- Eliminar columnas de status_history
ALTER TABLE status_history
DROP COLUMN delivery_latitude,
DROP COLUMN delivery_longitude;

-- Eliminar tabla tracking_history
DROP TABLE IF EXISTS tracking_history;
```

### Notas Importantes

1. **Backup**: Siempre haz un backup de la base de datos antes de ejecutar migraciones en producción
2. **Datos Existentes**: Las nuevas columnas permitirán valores NULL para registros existentes
3. **Índices**: Los índices creados mejorarán el rendimiento de búsquedas geoespaciales
4. **Foreign Keys**: Se han configurado con `ON DELETE CASCADE` y `ON DELETE SET NULL` según el caso

### Próximos Pasos

Después de ejecutar la migración:

1. ✅ Configurar la API Key de Google Maps en `config/google-maps-config.properties`
2. ✅ Iniciar Redis (si aún no está corriendo): `redis-server`
3. ✅ Ejecutar la aplicación Spring Boot
4. ✅ Geocodificar concesionarios existentes usando el endpoint `/dealerships/{id}/geocode`
