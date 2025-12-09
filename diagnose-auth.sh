#!/bin/bash

echo "==================================="
echo "DIAGNÓSTICO DE AUTENTICACIÓN"
echo "==================================="
echo ""

echo "1. Verificando usuarios en la base de datos..."
mysql -u root -p messenger -e "SELECT id_employee, document, user_name, role, LEFT(password, 10) as pass_preview FROM employees;" 2>/dev/null

echo ""
echo "2. Verificando concesionarios..."
mysql -u root -p messenger -e "SELECT id_dealership, name, zone FROM dealerships LIMIT 3;" 2>/dev/null

echo ""
echo "3. Para probar el login, usa este comando:"
echo "curl -X POST http://localhost:8080/auth/login \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '{\"userName\":\"admin\",\"password\":\"admin123\"}'"

echo ""
echo "4. Si el login funciona, deberías recibir un token JWT."
echo "   Copia el token y úsalo así:"
echo "curl -X POST http://localhost:8080/services/create \\"
echo "  -H 'Authorization: Bearer TU_TOKEN_AQUI' \\"
echo "  -F 'image=@ruta/a/tu/imagen.jpg' \\"
echo "  -F 'dealershipId=1' \\"
echo "  -F 'messengerDocument=222222222'"
