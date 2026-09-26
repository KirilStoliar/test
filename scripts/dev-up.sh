#!/usr/bin/env bash

set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
COMPOSE_FILE="$ROOT_DIR/docker/docker-compose.yml"
GRADLE_WRAPPER="$ROOT_DIR/gradlew"

cd "$ROOT_DIR"

echo "========================================"
echo " Service Orders - development startup"
echo "========================================"

echo
echo "[1/6] Checking Docker..."

if ! docker info >/dev/null 2>&1; then
echo "ERROR: Docker is not running or Docker CLI is unavailable."
echo "Start Docker and run the script again."
exit 1
fi

echo "Docker is available."

echo
echo "[2/6] Checking project files..."

if [ ! -f "$ENV_FILE" ]; then
echo "ERROR: .env file not found:"
echo "  $ENV_FILE"
echo
echo "Create it from .env.example:"
echo "  cp .env.example .env"
exit 1
fi

if [ ! -f "$COMPOSE_FILE" ]; then
echo "ERROR: Docker Compose file not found:"
echo "  $COMPOSE_FILE"
exit 1
fi

if [ ! -f "$GRADLE_WRAPPER" ]; then
echo "ERROR: Gradle wrapper not found:"
echo "  $GRADLE_WRAPPER"
exit 1
fi

echo ".env found."
echo "Docker Compose file found."
echo "Gradle wrapper found."

echo
echo "[3/6] Building services..."

"$GRADLE_WRAPPER" clean build -x test

echo
echo "[4/6] Starting Docker Compose..."

docker compose
--env-file "$ENV_FILE"
-f "$COMPOSE_FILE"
up -d --build

echo
echo "[5/6] Waiting for services..."

sleep 5

docker compose
--env-file "$ENV_FILE"
-f "$COMPOSE_FILE"
ps

echo
echo "[6/6] Available endpoints:"
echo
echo "  Auth Service:    http://localhost:8081"
echo "  User Service:    http://localhost:8082"
echo "  Product Service: http://localhost:8083"
echo
echo "  Auth Swagger:    http://localhost:8081/swagger-ui.html"
echo "  User Swagger:    http://localhost:8082/swagger-ui.html"
echo "  Product Swagger: http://localhost:8083/swagger-ui.html"
echo
echo "  Mailpit UI:      http://localhost:8025"
echo
echo "Startup completed."