# Service Orders Platform

Event-Driven Order Processing Platform для малого e-commerce магазина.

## 📋 О проекте

Платформа построена на микросервисной архитектуре с использованием современного стека технологий:

- **Java 21** + Spring Boot 3.5.6
- **Spring Security 6** + JWT/OAuth 2.1
- **Apache Kafka** + Schema Registry (Avro)
- **PostgreSQL 15** + MongoDB 7 + Redis 7
- **Docker** + **Kubernetes** (k3d)
- **Observability**: Prometheus, Grafana, Loki, Jaeger

### Архитектура сервисов

| Сервис | Назначение | БД | Протоколы |
|--------|------------|-----|-----------|
| **auth-service** | JWT/OAuth 2.1, refresh tokens | PostgreSQL | REST |
| **user-service** | CRUD пользователей, RBAC | PostgreSQL | REST, Kafka |
| **product-service** | Каталог товаров | MongoDB | REST, Kafka |
| **inventory-service** | Запасы и резервы | PostgreSQL | gRPC, Kafka |
| **order-service** | Жизненный цикл заказа | PostgreSQL | REST, gRPC, Kafka |
| **notification-service** | Email/SMS/push уведомления | MongoDB | Kafka |
| **api-gateway** | Единая точка входа | — | REST, gRPC |

---

## 🚀 Быстрый старт

### Требования

- **Docker Desktop** (с WSL2 backend на Windows)
- **Java 21** (для локальной сборки)
- **Git**
- **GNU Make** (опционально) или PowerShell 5+

### Доступные эндпоинты
Сервис	URL	Swagger UI
Auth Service	http://localhost:8081	http://localhost:8081/swagger-ui.html
User Service	http://localhost:8082	—

### При первом запуске автоматически создаётся администратор:
Email: admin@service-orders.com
Password: Admin123!

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd Service_orders
cp .env.example .env
./scripts/dev-up.sh