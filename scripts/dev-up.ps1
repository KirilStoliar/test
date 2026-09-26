$ErrorActionPreference = "Stop"

$RootDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$EnvFile = Join-Path $RootDir ".env"
$ComposeFile = Join-Path $RootDir "docker\docker-compose.yml"
$GradleWrapper = Join-Path $RootDir "gradlew.bat"

Set-Location $RootDir

Write-Host "========================================"
Write-Host " Service Orders - development startup"
Write-Host "========================================"

Write-Host ""
Write-Host "[1/6] Checking Docker..."

$dockerInfoExitCode = 0

try {
$previousErrorActionPreference = $ErrorActionPreference
$ErrorActionPreference = "Continue"

& docker info | Out-Host

$dockerInfoExitCode = $LASTEXITCODE

}
finally {
$ErrorActionPreference = $previousErrorActionPreference
}

if ($dockerInfoExitCode -ne 0) {
Write-Host ""
Write-Host "ERROR: Docker daemon is not available."
Write-Host ""
Write-Host "Check Docker directly with:"
Write-Host "  docker info"
Write-Host ""
Write-Host "Check Docker context with:"
Write-Host "  docker context ls"
exit $dockerInfoExitCode
}

Write-Host "Docker is available."

Write-Host ""
Write-Host "[2/6] Checking project files..."

if (-not (Test-Path -LiteralPath $EnvFile)) {
Write-Host "ERROR: .env file not found:"
Write-Host "  $EnvFile"
Write-Host ""
Write-Host "Create it from .env.example:"
Write-Host "  Copy-Item .env.example .env"
exit 1
}

if (-not (Test-Path -LiteralPath $ComposeFile)) {
Write-Host "ERROR: Docker Compose file not found:"
Write-Host "  $ComposeFile"
exit 1
}

if (-not (Test-Path -LiteralPath $GradleWrapper)) {
Write-Host "ERROR: Gradle wrapper not found:"
Write-Host "  $GradleWrapper"
exit 1
}

Write-Host ".env found."
Write-Host "Docker Compose file found."
Write-Host "Gradle wrapper found."

Write-Host ""
Write-Host "[3/6] Building services..."

& $GradleWrapper clean build -x test

if ($LASTEXITCODE -ne 0) {
Write-Host ""
Write-Host "ERROR: Gradle build failed."
exit $LASTEXITCODE
}

Write-Host ""
Write-Host "[4/6] Starting Docker Compose..."

$ComposeArguments = @(
"--env-file", $EnvFile,
"-f", $ComposeFile,
"up",
"-d",
"--build"
)

& docker compose @ComposeArguments

if ($LASTEXITCODE -ne 0) {
Write-Host ""
Write-Host "ERROR: Docker Compose startup failed."
exit $LASTEXITCODE
}

Write-Host ""
Write-Host "[5/6] Waiting for services..."

Start-Sleep -Seconds 5

$ComposePsArguments = @(
"--env-file", $EnvFile,
"-f", $ComposeFile,
"ps"
)

& docker compose @ComposePsArguments

if ($LASTEXITCODE -ne 0) {
Write-Host ""
Write-Host "ERROR: Failed to read Docker Compose service status."
exit $LASTEXITCODE
}

Write-Host ""
Write-Host "[6/6] Available endpoints:"
Write-Host ""
Write-Host "  Auth Service:    http://localhost:8081"
Write-Host "  User Service:    http://localhost:8082"
Write-Host "  Product Service: http://localhost:8083"
Write-Host ""
Write-Host "  Auth Swagger:    http://localhost:8081/swagger-ui.html"
Write-Host "  User Swagger:    http://localhost:8082/swagger-ui.html"
Write-Host "  Product Swagger: http://localhost:8083/swagger-ui.html"
Write-Host ""
Write-Host "  Mailpit UI:      http://localhost:8025"
Write-Host ""
Write-Host "Startup completed."