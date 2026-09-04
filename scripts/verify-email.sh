#!/bin/bash

# Скрипт для автоматической верификации email

echo "========================================"
echo "  Email Verification Helper"
echo "========================================"
echo

# Получаем последний токен из логов
TOKEN=$(docker logs service-orders-auth-service 2>&1 | grep "EMAIL VERIFICATION TOKEN" | tail -1 | awk -F': ' '{print $2}')

if [ -z "$TOKEN" ]; then
    echo "No verification token found in logs."
    echo "Make sure you have registered a user first."
    exit 1
fi

echo "✅ Found verification token: $TOKEN"
echo

# Отправляем запрос на верификацию
echo "Sending verification request..."
RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/verify \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}")

echo
echo "Response: $RESPONSE"
echo

# Проверяем успешность
if echo "$RESPONSE" | grep -q "successfully"; then
    echo "Email successfully verified!"
    echo
    echo "You can now login:"
    echo "  curl -X POST http://localhost:8081/api/v1/auth/login \\"
    echo "    -H 'Content-Type: application/json' \\"
    echo "    -d '{\"email\":\"your_email\",\"password\":\"your_password\"}'"
else
    echo "Verification failed. Response: $RESPONSE"
fi