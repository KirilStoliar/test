param(
    [string]$Email = "test$(Get-Date -Format 'yyyyMMddHHmmss')@example.com",
    [string]$Password = "Password123!"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Register and Verify Helper" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Email: $Email" -ForegroundColor Yellow
Write-Host "Password: $Password" -ForegroundColor Yellow
Write-Host ""

# 1. Регистрация
Write-Host "[1/3] Registering user..." -ForegroundColor Yellow
$registerBody = @{ email = $Email; password = $Password } | ConvertTo-Json
$registerResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/register" `
    -Method Post `
    -ContentType "application/json" `
    -Body $registerBody

Write-Host "Register response: $($registerResponse | ConvertTo-Json)" -ForegroundColor White
Write-Host ""

# 2. Получение токена
Write-Host "[2/3] Getting verification token from logs..." -ForegroundColor Yellow
Start-Sleep -Seconds 2

$logs = docker logs service-orders-auth-service 2>&1
$tokenLine = $logs | Select-String "EMAIL VERIFICATION TOKEN" | Select-Object -Last 1

if (-not $tokenLine) {
    Write-Host "No verification token found." -ForegroundColor Red
    exit 1
}

$token = $tokenLine.ToString().Split(": ")[-1].Trim()
Write-Host "Found token: $token" -ForegroundColor Green
Write-Host ""

# 3. Верификация
Write-Host "[3/3] Verifying email..." -ForegroundColor Yellow
$verifyBody = @{ token = $token } | ConvertTo-Json
$verifyResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/verify" `
    -Method Post `
    -ContentType "application/json" `
    -Body $verifyBody

Write-Host "Verify response: $($verifyResponse | ConvertTo-Json)" -ForegroundColor White
Write-Host ""

# 4. Логин
Write-Host "Attempting login..." -ForegroundColor Yellow
$loginBody = @{ email = $Email; password = $Password } | ConvertTo-Json
try {
    $loginResponse = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/login" `
        -Method Post `
        -ContentType "application/json" `
        -Body $loginBody

    Write-Host "Login response: $($loginResponse | ConvertTo-Json)" -ForegroundColor White
    Write-Host ""

    if ($loginResponse.accessToken) {
        Write-Host "SUCCESS! User registered, verified and logged in!" -ForegroundColor Green
        Write-Host ""
        Write-Host "Access Token: $($loginResponse.accessToken)" -ForegroundColor Cyan
        Write-Host "Refresh Token: $($loginResponse.refreshToken)" -ForegroundColor Cyan
    }
} catch {
    Write-Host "Login failed: $($_.Exception.Message)" -ForegroundColor Red
}