#!/bin/bash

# Script para probar el rate limiting del login
# Uso: ./test-rate-limiting.sh

API_URL="${1:-http://localhost:8080}"
DOCUMENT="123456"
WRONG_PASSWORD="wrongpassword123"

echo "=========================================="
echo "Rate Limiting Test Script"
echo "=========================================="
echo "API URL: $API_URL"
echo "Testing with document: $DOCUMENT"
echo ""

test_login() {
    local attempt=$1
    echo "Attempt $attempt:"
    response=$(curl -s -w "\n%{http_code}" -X POST "$API_URL/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"document\": $DOCUMENT, \"password\": \"$WRONG_PASSWORD\", \"turnstileToken\": \"dummy-token-for-testing\"}")
    
    http_code=$(echo "$response" | tail -n 1)
    body=$(echo "$response" | head -n -1)
    
    echo "HTTP Status: $http_code"
    echo "Response: $body"
    echo ""
    
    if [ "$http_code" = "429" ]; then
        echo "✓ Account blocked after $attempt attempts (as expected)"
        return 1
    elif [ "$http_code" = "401" ]; then
        remaining=$(echo "$body" | grep -o 'Intentos restantes: [0-9]*' || echo "N/A")
        echo "✓ Invalid credentials. $remaining"
        return 0
    else
        echo "✗ Unexpected HTTP status: $http_code"
        return 2
    fi
}

echo "Test 1: Attempting login 6 times with wrong password"
echo "======================================================="

for i in {1..6}; do
    test_login $i
    status=$?
    
    if [ $status -eq 1 ]; then
        echo ""
        echo "✓ Test passed: Account blocked after 5 failed attempts"
        exit 0
    elif [ $status -eq 2 ]; then
        echo ""
        echo "✗ Test failed: Unexpected error"
        exit 2
    fi
    
    # Small delay between requests
    sleep 1
done

echo ""
echo "✗ Test failed: Account should have been blocked"
exit 1
