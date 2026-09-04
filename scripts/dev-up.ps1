$ErrorActionPreference = "Stop"

$RootDir = Resolve-Path (Join-Path $PSScriptRoot "..")

Set-Location $RootDir

Write-Host "========================================"
Write-Host " Service Orders - development startup"
Write-Host "========================================"

Write-Host ""
Write-Host "[1/6] Checking Docker..."

docker info | Out-Null

Write-Host "Docker is available."

Write-Host ""
Write-Host "[2/6] Checking .env..."

if (-not (Test-Path ".env")) {
    Write-Host "ERROR: .env file not found."
    Write-Host "Create it from .env.example:"
    Write-Host "  Copy-Item .env.example .env"
    exit 1
}

Write-Host ".env found."

Write-Host ""
Write-Host "[3/6] Building services..."

.\gradlew.bat clean build -x test

Write-Host ""
Write-Host "[4/6] Starting Docker Compose..."

docker compose -f docker/docker-compose.yml up -d --build

Write-Host ""
Write-Host "[5/6] Waiting for services..."

Start-Sleep -Seconds 5

docker compose -f docker/docker-compose.yml ps

Write-Host ""
Write-Host "[6/6] Available endpoints:"
Write-Host ""
Write-Host "  Auth Service: http://localhost:8081"
Write-Host "  User Service: http://localhost:8082"
Write-Host ""
Write-Host "Startup completed."