#!/bin/bash

# Полный цикл регистрации и верификации
# Использование: ./scripts/register-and-verify.sh email password

EMAIL=${1:-"test$(date +%s)@example.com"}
PASSWORD=${2:-"Password123!"}

echo "========================================"
echo "  Register and Verify Helper"
echo "========================================"
echo
echo "Email: $EMAIL"
echo "Password: $PASSWORD"
echo

# 1. Регистрация
echo "[1/3] Registering user..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

echo "Register response: $REGISTER_RESPONSE"
echo

# 2. Получение токена из логов
echo "[2/3] Getting verification token from logs..."
sleep 2

TOKEN=$(docker logs service-orders-auth-service 2>&1 | grep "EMAIL VERIFICATION TOKEN" | tail -1 | awk -F': ' '{print $2}')

if [ -z "$TOKEN" ]; then
    echo "No verification token found. Please check logs."
    exit 1
fi

echo "Found token: $TOKEN"
echo

# 3. Верификация
echo "[3/3] Verifying email..."
VERIFY_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/verify \
  -H "Content-Type: application/json" \
  -d "{\"token\":\"$TOKEN\"}")

echo "Verify response: $VERIFY_RESPONSE"
echo

# 4. Логин
echo "Attempting login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")

echo "Login response: $LOGIN_RESPONSE"
echo

if echo "$LOGIN_RESPONSE" | grep -q "accessToken"; then
    echo "SUCCESS! User registered, verified and logged in!"
else
    echo "Login failed. Response: $LOGIN_RESPONSE"
fi