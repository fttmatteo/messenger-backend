#!/bin/bash

# Este script prueba que los headers de seguridad se configuraron correctamente

echo "🔒 SPRINT 1 - Verificación de Security Headers"
echo "=============================================="
echo ""

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

BASE_URL="${API_URL:-http://localhost:8080}"

echo "📍 Testeando servidor: $BASE_URL"
echo ""

check_header() {
    local header_name="$1"
    local expected_value="$2"
    local response=$(curl -s -I "$BASE_URL/actuator/health" 2>/dev/null | grep -i "$header_name")
    
    if [ -n "$response" ]; then
        echo -e "${GREEN}✓${NC} $header_name: Presente"
        echo "  → $response"
        return 0
    else
        echo -e "${RED}✗${NC} $header_name: NO encontrado"
        return 1
    fi
}

echo "1️⃣  Verificando conectividad..."
if curl -s -f "$BASE_URL/actuator/health" > /dev/null 2>&1; then
    echo -e "${GREEN}✓${NC} Servidor accesible\n"
else
    echo -e "${RED}✗${NC} Servidor NO accesible en $BASE_URL"
    echo "   Asegúrate de que la aplicación esté corriendo"
    exit 1
fi

echo "2️⃣  Verificando Security Headers HTTP..."
echo ""

passed=0
failed=0

if check_header "X-Content-Type-Options" "nosniff"; then
    ((passed++))
else
    ((failed++))
fi
echo ""

if check_header "X-XSS-Protection" "1; mode=block"; then
    ((passed++))
else
    ((failed++))
fi
echo ""

if check_header "X-Frame-Options" "DENY"; then
    ((passed++))
else
    ((failed++))
fi
echo ""

if [[ $BASE_URL == https://* ]]; then
    if check_header "Strict-Transport-Security" "max-age=31536000"; then
        ((passed++))
    else
        ((failed++))
    fi
    echo ""
fi

if check_header "Content-Security-Policy" "default-src"; then
    ((passed++))
else
    ((failed++))
fi
echo ""

echo "3️⃣  Verificando CORS Configuration..."
echo ""

cors_response=$(curl -s -I -X OPTIONS \
    -H "Origin: http://localhost:5173" \
    -H "Access-Control-Request-Method: POST" \
    -H "Access-Control-Request-Headers: Authorization,Content-Type" \
    "$BASE_URL/auth/login" 2>/dev/null)

if echo "$cors_response" | grep -qi "Access-Control-Allow-Origin"; then
    echo -e "${GREEN}✓${NC} CORS: Access-Control-Allow-Origin presente"
    ((passed++))
else
    echo -e "${RED}✗${NC} CORS: Access-Control-Allow-Origin NO encontrado"
    ((failed++))
fi

if echo "$cors_response" | grep -qi "Access-Control-Allow-Headers"; then
    allowed_headers=$(echo "$cors_response" | grep -i "Access-Control-Allow-Headers" | cut -d: -f2)
    echo -e "${GREEN}✓${NC} CORS: Headers permitidos configurados"
    echo "  → $allowed_headers"
    
    if echo "$allowed_headers" | grep -q "\*"; then
        echo -e "${YELLOW}⚠${NC}  Advertencia: Se permiten TODOS los headers (*)"
        echo "     Recomendación: Restringir a lista específica"
    else
        echo -e "${GREEN}✓${NC} Headers restringidos (más seguro que '*')"
    fi
    
    if echo "$allowed_headers" | grep -qi "X-Correlation-Id"; then
        echo -e "${GREEN}✓${NC} CORS: X-Correlation-Id permitido correctamente"
    else
        echo -e "${RED}✗${NC} CORS: X-Correlation-Id NO encontrado en headers permitidos"
        ((failed++))
    fi
    
    ((passed++))
else
    echo -e "${RED}✗${NC} CORS: Access-Control-Allow-Headers NO encontrado"
    ((failed++))
fi
echo ""

echo "4️⃣  Verificando CSRF Configuration..."
echo ""

csrf_response=$(curl -s -I "$BASE_URL/ws/info" 2>/dev/null)

if echo "$csrf_response" | grep -qi "X-CSRF-TOKEN\|Set-Cookie.*XSRF-TOKEN"; then
    echo -e "${GREEN}✓${NC} CSRF: Token disponible para endpoints que lo requieren"
    ((passed++))
else
    echo -e "${YELLOW}⚠${NC}  CSRF: No se detectó token (puede ser normal para endpoints excluidos)"
fi
echo ""

echo "=============================================="
echo "📊 RESUMEN DE TESTS"
echo "=============================================="
echo ""
echo -e "Tests pasados:  ${GREEN}$passed${NC}"
echo -e "Tests fallidos: ${RED}$failed${NC}"
echo ""

if [ $failed -eq 0 ]; then
    echo -e "${GREEN}✅ SPRINT 1 COMPLETADO EXITOSAMENTE${NC}"
    echo ""
    echo "Próximos pasos:"
    echo "1. Desplegar a staging/producción"
    echo "2. Verificar headers con: https://securityheaders.com"
    echo "3. Continuar con SPRINT 2 (Cookies HttpOnly)"
    exit 0
else
    echo -e "${YELLOW}⚠️  ALGUNOS TESTS FALLARON${NC}"
    echo ""
    echo "Acciones requeridas:"
    echo "1. Verificar que la aplicación esté corriendo"
    echo "2. Revisar configuración en SecurityConfig.java"
    echo "3. Reiniciar la aplicación si hiciste cambios"
    exit 1
fi
