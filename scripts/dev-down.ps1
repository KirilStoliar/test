$ErrorActionPreference = "Stop"

Set-Location (Join-Path $PSScriptRoot "..")

Write-Host "Stopping Service Orders..."

docker compose -f docker/docker-compose.yml down

Write-Host "Service Orders stopped."