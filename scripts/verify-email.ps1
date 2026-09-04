# Скрипт для автоматической верификации email
# Использование: .\scripts\verify-email.ps1

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Email Verification Helper" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Получаем последний токен из логов
$logs = docker logs service-orders-auth-service 2>&1
$tokenLine = $logs | Select-String "EMAIL VERIFICATION TOKEN" | Select-Object -Last 1

if (-not $tokenLine) {
    Write-Host "No verification token found in logs." -ForegroundColor Red
    Write-Host "Make sure you have registered a user first." -ForegroundColor Yellow
    exit 1
}

$token = $tokenLine.ToString().Split(": ")[-1].Trim()
Write-Host "Found verification token: $token" -ForegroundColor Green
Write-Host ""

# Отправляем запрос на верификацию
Write-Host "Sending verification request..." -ForegroundColor Yellow
$body = @{ token = $token } | ConvertTo-Json
$response = Invoke-RestMethod -Uri "http://localhost:8081/api/v1/auth/verify" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body

Write-Host ""
Write-Host "Response: $($response | ConvertTo-Json)" -ForegroundColor White
Write-Host ""

# Проверяем успешность
if ($response.message -match "successfully") {
    Write-Host "Email successfully verified!" -ForegroundColor Green
    Write-Host ""
    Write-Host "You can now login:" -ForegroundColor Yellow
    Write-Host "  curl -X POST http://localhost:8081/api/v1/auth/login \" -ForegroundColor Gray
    Write-Host "    -H 'Content-Type: application/json' \" -ForegroundColor Gray
    Write-Host "    -d '{\"email\":\"your_email\",\"password\":\"your_password\"}'" -ForegroundColor Gray
} else {
    Write-Host "Verification failed." -ForegroundColor Red
}