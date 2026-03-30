#!/bin/bash

# Script para verificar si Flyway está funcionando
# Ejecuta este script para conectarte a tu base de datos MySQL y verificar

echo "=== VERIFICACIÓN DE FLYWAY ==="
echo ""
echo "Por favor ingresa los datos de conexión a tu base de datos:"
read -p "Host (default: localhost): " DB_HOST
DB_HOST=${DB_HOST:-localhost}
read -p "Puerto (default: 3306): " DB_PORT
DB_PORT=${DB_PORT:-3306}
read -p "Database name (default: messenger_dev): " DB_NAME
DB_NAME=${DB_NAME:-messenger_dev}
read -p "Username (default: root): " DB_USER
DB_USER=${DB_USER:-root}
read -p "Password (default: secret123): " -s DB_PASS
DB_PASS=${DB_PASS:-secret123}
echo ""
echo ""

echo "Conectando a la base de datos..."
echo ""

# Verificar si existe la tabla flyway_schema_history
echo "1. Verificando si existe tabla flyway_schema_history..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
  -e "SHOW TABLES LIKE 'flyway_schema_history';" 2>/dev/null

if [ $? -eq 0 ]; then
  echo "Tabla flyway_schema_history encontrada"
  echo ""
  
  echo "2. Contenido de flyway_schema_history:"
  mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
    -e "SELECT installed_rank, version, description, type, script, installed_on, success FROM flyway_schema_history;" 2>/dev/null
  
  if [ $? -eq 0 ]; then
    echo ""
    echo "FLYWAY ESTÁ FUNCIONANDO CORRECTAMENTE"
  else
    echo "Error al leer flyway_schema_history"
  fi
else
  echo "Tabla flyway_schema_history NO encontrada"
  echo ""
  echo "Esto significa que Flyway NO se ha ejecutado."
  echo "Las tablas se crearon con Hibernate (ddl-auto=update)"
fi

echo ""
echo "3. Verificando archivos de migración locales..."
MIGRATION_PATH="src/main/resources/db/migration"
if [ -d "$MIGRATION_PATH" ]; then
  count=$(ls -1 "$MIGRATION_PATH"/*.sql 2>/dev/null | wc -l)
  echo "Se encontraron $count archivos de migración en $MIGRATION_PATH"
  ls -1 "$MIGRATION_PATH"/*.sql | xargs -n 1 basename | tail -n 5
else
  echo "Carpeta de migraciones NO encontrada en $MIGRATION_PATH"
fi

echo ""
echo "4. Verificando tablas de la aplicación..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
  -e "SHOW TABLES;" 2>/dev/null

echo ""
echo "=== FIN DE LA VERIFICACIÓN ==="
