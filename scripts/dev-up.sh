#!/usr/bin/env bash

set -e

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

cd "$ROOT_DIR"

echo "========================================"
echo " Service Orders - development startup"
echo "========================================"

echo
echo "[1/6] Checking Docker..."

if ! docker info >/dev/null 2>&1; then
    echo "ERROR: Docker is not running."
    exit 1
fi

echo "Docker is available."

echo
echo "[2/6] Checking .env..."

if [ ! -f ".env" ]; then
    echo "ERROR: .env file not found."
    echo "Create it from .env.example:"
    echo "  cp .env.example .env"
    exit 1
fi

echo ".env found."

echo
echo "[3/6] Building services..."

./gradlew clean build -x test

echo
echo "[4/6] Starting Docker Compose..."

docker compose \
    --env-file "$ROOT_DIR/.env" \
    -f "$ROOT_DIR/docker/docker-compose.yml" \
    up -d --build

echo
echo "[5/6] Waiting for services..."

sleep 5

docker compose \
    --env-file "$ROOT_DIR/.env" \
    -f "$ROOT_DIR/docker/docker-compose.yml" \
    ps

echo
echo "[6/6] Available endpoints:"
echo
echo "  Auth Service: http://localhost:8081"
echo "  User Service: http://localhost:8082"
echo "  Product Service: http://localhost:8083"
echo
echo "Startup completed."