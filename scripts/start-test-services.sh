#!/bin/bash
# Script to start integration test Docker services
# Run from project root: ./scripts/start-test-services.sh

COMPOSE_FILE="messenger/src/test/resources/compose.yml"

echo "🚀 Starting integration test services..."
docker compose -f "$COMPOSE_FILE" up -d

echo ""
echo "⏳ Waiting for MySQL to be healthy..."
while ! docker compose -f "$COMPOSE_FILE" ps mysql 2>/dev/null | grep -q "healthy"; do
    echo -n "."
    sleep 2
done
echo " ✅"

echo ""
echo "📊 Services status:"
docker compose -f "$COMPOSE_FILE" ps

echo ""
echo "🔌 Connection info:"
echo "   MySQL: localhost:3307"
echo "   Redis: localhost:6380"
echo ""
echo "🧪 Run tests with: cd messenger && ./mvnw test"
echo "🛑 Stop services with: docker compose -f $COMPOSE_FILE down"
