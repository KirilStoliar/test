#!/usr/bin/env bash

set -e

cd "$(dirname "$0")/.."

echo "Stopping Service Orders..."

docker compose -f docker/docker-compose.yml down

echo "Service Orders stopped."