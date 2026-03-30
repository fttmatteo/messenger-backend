#!/bin/bash
# Script to start integration test Docker services
# Run from project root: ./scripts/start-test-services.sh

COMPOSE_FILE="docker-compose.local.yml"

echo "🚀 Starting integration test services..."
docker compose -f "$COMPOSE_FILE" up -d messenger-db messenger-redis

echo ""
echo "⏳ Waiting for MySQL to be healthy..."
while ! docker compose -f "$COMPOSE_FILE" ps messenger-db 2>/dev/null | grep -q "healthy"; do
    echo -n "."
    sleep 2
done
echo " ✅"

echo ""
echo "📊 Services status:"
docker compose -f "$COMPOSE_FILE" ps messenger-db messenger-redis

echo ""
echo "🔌 Connection info:"
echo "   MySQL: localhost:3306"
echo "   Redis: localhost:6379"
echo ""
echo "🧪 Run tests with: cd messenger && ./mvnw test"
echo "🛑 Stop services with: docker compose -f $COMPOSE_FILE stop messenger-db messenger-redis"
