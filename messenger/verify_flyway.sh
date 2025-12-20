#!/bin/bash

# Script para verificar si Flyway está funcionando
# Ejecuta este script para conectarte a tu base de datos MySQL y verificar

echo "=== VERIFICACIÓN DE FLYWAY ==="
echo ""
echo "Por favor ingresa los datos de conexión a tu base de datos:"
read -p "Host (ej: your-db-host.aivencloud.com): " DB_HOST
read -p "Puerto (default: 3306): " DB_PORT
DB_PORT=${DB_PORT:-3306}
read -p "Database name: " DB_NAME
read -p "Username: " DB_USER
read -sp "Password: " DB_PASS
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
echo "3. Verificando tablas de la aplicación..."
mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" \
  -e "SHOW TABLES;" 2>/dev/null

echo ""
echo "=== FIN DE LA VERIFICACIÓN ==="
