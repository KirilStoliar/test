# Service Orders

Event-driven microservices platform for order processing in a small e-commerce application.

The project is an educational implementation focused on Java 21, Spring Boot 3, Spring Security, JWT/OAuth2, Kafka/Avro, PostgreSQL, MongoDB, Redis and Docker.

The project is developed incrementally. The current local development stack contains the authentication, user and product services together with the required infrastructure.

---

## Contents

* [Current project status](#current-project-status)
* [Technology stack](#technology-stack)
* [Architecture](#architecture)
* [Requirements](#requirements)
* [Quick start](#quick-start)

    * [Linux / macOS](#linux--macos)
    * [Git Bash on Windows](#git-bash-on-windows)
    * [Windows PowerShell](#windows-powershell)
* [Environment configuration](#environment-configuration)
* [Docker Compose](#docker-compose)
* [Services and ports](#services-and-ports)
* [Swagger / OpenAPI](#swagger--openapi)
* [Mailpit](#mailpit)
* [Health checks](#health-checks)
* [Authentication](#authentication)
* [Product service](#product-service)
* [Useful Docker commands](#useful-docker-commands)
* [Stopping the application](#stopping-the-application)
* [Rebuilding the application](#rebuilding-the-application)
* [Troubleshooting](#troubleshooting)
* [Project structure](#project-structure)
* [Development](#development)

---

# Current project status

The current development stack includes:

### Application services

| Service           |   Port | Main responsibility                                                         | Storage    |
| ----------------- | -----: | --------------------------------------------------------------------------- | ---------- |
| `auth-service`    | `8081` | Registration, email verification, JWT, refresh tokens, OAuth2 configuration | PostgreSQL |
| `user-service`    | `8082` | User data and RBAC                                                          | PostgreSQL |
| `product-service` | `8083` | Product catalog and product search/filtering                                | MongoDB    |

### Infrastructure

| Component          |    Port | Purpose                               |
| ------------------ | ------: | ------------------------------------- |
| PostgreSQL         |  `5432` | Relational storage for auth and users |
| MongoDB            | `27017` | Product catalog storage               |
| Redis              |  `6379` | Redis infrastructure                  |
| Redpanda / Kafka   | `19092` | Event streaming                       |
| Schema Registry    | `18081` | Avro schema registry                  |
| Redpanda Admin API | `19644` | Redpanda administration               |
| Mailpit SMTP       |  `1025` | Local SMTP server                     |
| Mailpit Web UI     |  `8025` | Local email inspection                |

The following services belong to the planned architecture but are not currently started by the development Compose stack:

* `inventory-service`
* `order-service`
* `notification-service`
* `api-gateway`

They should not be considered available through the current `dev-up` scripts.

---

# Technology stack

* Java 21
* Spring Boot 3.5.6
* Spring Security 6
* JWT
* OAuth 2.1 / OAuth2 client configuration
* Spring Data JPA
* Spring Data MongoDB
* PostgreSQL 15
* MongoDB 7
* Redis 7
* Apache Kafka API through Redpanda
* Apache Avro
* Confluent Schema Registry
* Liquibase
* Gradle Kotlin DSL
* Docker
* Docker Compose
* SpringDoc OpenAPI / Swagger UI
* Mailpit for local email testing

---

# Architecture

The current application consists of three implemented application services.

## auth-service

Responsible for:

* user registration;
* email verification;
* login;
* JWT access tokens;
* refresh tokens;
* refresh token rotation;
* logout / refresh token revocation;
* role assignment;
* OAuth2 client configuration;
* publishing user creation events.

The service uses PostgreSQL.

## user-service

Responsible for:

* storing user data;
* retrieving users;
* updating users;
* deleting users according to role permissions;
* role-based access control;
* consuming `user.created` Kafka events.

The service uses PostgreSQL and Liquibase.

## product-service

Responsible for:

* product creation;
* product update;
* product deletion;
* publishing/hiding products;
* product search;
* category filtering;
* price filtering;
* pagination;
* sorting.

The service uses MongoDB.

Product prices are stored as MongoDB `Decimal128` values to support correct numeric range filtering.

---

# Requirements

Before starting the project, install:

* Git
* Java 21
* Docker Desktop on Windows
* Docker Engine + Docker Compose on Linux
* Bash for Linux/macOS/Git Bash
* PowerShell 5+ for Windows PowerShell

Verify the installations.

## Java

```bash
java -version
```

Expected major version:

```text
21
```

## Git

```bash
git --version
```

## Docker

```bash
docker --version
docker compose version
```

Docker must be running before starting the application.

---

# Quick start

The recommended way to start the project is through one of the provided scripts.

The scripts expect `.env` in the **repository root**.

The repository contains:

```text
.env.example
```

Create the local configuration first.

---

## Linux / macOS

Clone the repository:

```bash
git clone https://github.com/KirilStoliar/test.git
cd test
```

Create `.env`:

```bash
cp .env.example .env
```

Edit `.env` if necessary.

Make the startup script executable:

```bash
chmod +x scripts/dev-up.sh
```

Start the application:

```bash
./scripts/dev-up.sh
```

The script:

1. checks Docker;
2. checks `.env`;
3. checks the required project files;
4. builds the Gradle project;
5. starts Docker Compose;
6. displays the running containers;
7. prints the main application URLs.

---

## Git Bash on Windows

Open Git Bash in the repository root.

Clone the repository if necessary:

```bash
git clone https://github.com/KirilStoliar/test.git
cd test
```

Create `.env`:

```bash
cp .env.example .env
```

Start the application:

```bash
./scripts/dev-up.sh
```

The script uses the repository root to locate `.env` and explicitly passes it to Docker Compose.

---

## Windows PowerShell

Open PowerShell in the repository root.

Clone the repository if necessary:

```powershell
git clone https://github.com/KirilStoliar/test.git
cd test
```

Create `.env`:

```powershell
Copy-Item .env.example .env
```

Start the application:

```powershell
.\scripts\dev-up.ps1
```

The PowerShell script explicitly passes the root `.env` to Docker Compose:

```text
docker compose --env-file .env ...
```

Therefore the startup does not depend on a `docker/.env` file.

---

# Environment configuration

The project uses:

```text
.env
```

in the repository root.

Do not commit your personal `.env` file.

Use:

```text
.env.example
```

as the template.

## PostgreSQL

```dotenv
POSTGRES_USER=service_orders
POSTGRES_PASSWORD=service_orders_dev
POSTGRES_DB=service_orders
```

The Compose file creates the PostgreSQL server and the application databases required by the services.

## MongoDB

```dotenv
MONGO_ROOT_USERNAME=service_orders
MONGO_ROOT_PASSWORD=mongo_dev_password
```

The MongoDB container uses these values as the root credentials.

## Redis

```dotenv
REDIS_PASSWORD=redis_dev_password
```

## JWT

```dotenv
JWT_SECRET=CHANGE_ME_TO_A_RANDOM_SECRET_AT_LEAST_32_CHARACTERS
```

For local development, replace this value with a random secret of at least 32 characters.

Do not use a development secret in production.

## Bootstrap administrator

```dotenv
ADMIN_EMAIL=
ADMIN_PASSWORD=
ADMIN_ENABLED=true
```

If administrator bootstrap is enabled, provide the administrator credentials in `.env`.

Do not put production credentials into `.env.example`.

## Mailpit

The local development environment uses Mailpit:

```dotenv
MAIL_ENABLED=true
MAIL_HOST=mailpit
MAIL_PORT=1025
MAIL_USERNAME=
MAIL_PASSWORD=
MAIL_SMTP_AUTH=false
MAIL_SMTP_STARTTLS=false
MAIL_FROM=no-reply@service-orders.local
MAIL_VERIFICATION_BASE_URL=http://localhost:8081/api/v1/auth/verify
```

No external SMTP account is required for local development.

## Google OAuth2

```dotenv
GOOGLE_CLIENT_ID=CHANGE_ME
GOOGLE_CLIENT_SECRET=CHANGE_ME
```

## GitHub OAuth2

```dotenv
GITHUB_CLIENT_ID=CHANGE_ME
GITHUB_CLIENT_SECRET=CHANGE_ME
```

OAuth2 credentials are optional for local work that does not test Google/GitHub login.

---

# Docker Compose

The Compose file is:

```text
docker/docker-compose.yml
```

When using it directly, explicitly specify the root environment file:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  up -d --build
```

Check the containers:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  ps
```

View logs:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs -f
```

View logs for one service:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs -f auth-service
```

---

# Services and ports

## Auth Service

Base URL:

```text
http://localhost:8081
```

Health:

```text
http://localhost:8081/actuator/health
```

Swagger:

```text
http://localhost:8081/swagger-ui.html
```

OpenAPI:

```text
http://localhost:8081/v3/api-docs
```

## User Service

Base URL:

```text
http://localhost:8082
```

Health:

```text
http://localhost:8082/actuator/health
```

Swagger:

```text
http://localhost:8082/swagger-ui.html
```

OpenAPI:

```text
http://localhost:8082/v3/api-docs
```

## Product Service

Base URL:

```text
http://localhost:8083
```

Health:

```text
http://localhost:8083/actuator/health
```

Swagger:

```text
http://localhost:8083/swagger-ui.html
```

OpenAPI:

```text
http://localhost:8083/v3/api-docs
```

---

# Swagger / OpenAPI

Swagger UI is available for all three current application services.

Open:

```text
http://localhost:8081/swagger-ui.html
```

```text
http://localhost:8082/swagger-ui.html
```

```text
http://localhost:8083/swagger-ui.html
```

The OpenAPI JSON documents are available at:

```text
http://localhost:8081/v3/api-docs
```

```text
http://localhost:8082/v3/api-docs
```

```text
http://localhost:8083/v3/api-docs
```

The API documentation is intended for local development and manual API testing.

---

# Mailpit

Mailpit is used as the local SMTP server for email verification.

Web interface:

```text
http://localhost:8025
```

SMTP:

```text
localhost:1025
```

When a user registers, the verification email is sent to Mailpit instead of an external mailbox.

Open:

```text
http://localhost:8025
```

and inspect the received message.

The verification URL points to:

```text
http://localhost:8081/api/v1/auth/verify
```

---

# Health checks

After startup, check the three application services.

## Auth

```bash
curl.exe -i http://localhost:8081/actuator/health
```

Linux/macOS:

```bash
curl -i http://localhost:8081/actuator/health
```

Expected status:

```text
HTTP/1.1 200
```

with:

```json
{
  "status": "UP"
}
```

## User

```bash
curl.exe -i http://localhost:8082/actuator/health
```

## Product

```bash
curl.exe -i http://localhost:8083/actuator/health
```

On Linux/macOS use `curl` instead of `curl.exe`.

The infrastructure containers can be checked with:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  ps
```

PostgreSQL, MongoDB, Redis and Redpanda have Docker health checks configured.

---

# Authentication

The authentication API is available through:

```text
http://localhost:8081
```

Main operations include:

* registration;
* email verification;
* login;
* refresh;
* logout;
* current authenticated user.

Registration example:

```bash
curl.exe -i -X POST \
  "http://localhost:8081/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"test@mail.com","password":"String123!"}'
```

After registration, open Mailpit:

```text
http://localhost:8025
```

and use the verification link from the email.

After verification, login can be performed through:

```text
POST /api/v1/auth/login
```

The response contains an access token and refresh token.

The access token is used with:

```text
Authorization: Bearer <access-token>
```

The refresh token is used by:

```text
POST /api/v1/auth/refresh
```

Logout revokes the supplied refresh token:

```text
POST /api/v1/auth/logout
```

Invalid or unknown refresh tokens are rejected instead of being silently accepted.

---

# Product Service

Product API:

```text
http://localhost:8083/api/v1/products
```

The service supports:

* create product;
* update product;
* delete product;
* publish product;
* hide product;
* get product;
* search by text;
* category filtering;
* minimum price filtering;
* maximum price filtering;
* combined price filtering;
* pagination;
* sorting.

Product mutations require administrator permissions.

Example:

```bash
curl.exe -i \
  "http://localhost:8083/api/v1/products" \
  -H "Authorization: Bearer $USER_TOKEN"
```

Price filtering example:

```bash
curl.exe -i \
  "http://localhost:8083/api/v1/products?category=electronics&minPrice=900&maxPrice=1100" \
  -H "Authorization: Bearer $USER_TOKEN"
```

Text + category + price filtering:

```bash
curl.exe -i \
  "http://localhost:8083/api/v1/products?query=Price%20Filter&category=electronics&minPrice=900&maxPrice=1100" \
  -H "Authorization: Bearer $USER_TOKEN"
```

Product prices are persisted in MongoDB as Decimal128 values so that numeric range queries use numeric comparison rather than string comparison.

---

# Useful Docker commands

Show all containers:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  ps
```

Follow all logs:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs -f
```

Follow auth-service logs:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs -f auth-service
```

Follow user-service logs:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs -f user-service
```

Follow product-service logs:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs -f product-service
```

Restart one service:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  restart auth-service
```

---

# Stopping the application

Stop the containers without deleting volumes:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  down
```

This stops the application while keeping persistent Docker volumes.

To stop and remove the containers and their volumes:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  down -v
```

The `-v` option deletes local development database data.

Use it only when you intentionally want to recreate PostgreSQL, MongoDB, Redis and Redpanda data.

---

# Rebuilding the application

Build the project locally:

```bash
./gradlew clean build -x test
```

Windows:

```powershell
.\gradlew.bat clean build -x test
```

Rebuild and restart Docker services:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  up -d --build
```

Or use the startup script again.

Linux / Git Bash:

```bash
./scripts/dev-up.sh
```

Windows PowerShell:

```powershell
.\scripts\dev-up.ps1
```

---

# Troubleshooting

## `.env file not found`

Create it from the example.

Linux / Git Bash:

```bash
cp .env.example .env
```

PowerShell:

```powershell
Copy-Item .env.example .env
```

The `.env` file must be located in the repository root:

```text
test/
├── .env
├── .env.example
├── README.md
├── gradlew
├── gradlew.bat
├── scripts/
└── docker/
```

---

## Docker is not running

Start Docker Desktop on Windows or Docker Engine on Linux.

Verify:

```bash
docker info
```

Then run the startup script again.

---

## PostgreSQL health check fails

Check that these variables are present in `.env`:

```dotenv
POSTGRES_USER=service_orders
POSTGRES_PASSWORD=service_orders_dev
POSTGRES_DB=service_orders
```

Then inspect:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs postgres
```

If an old PostgreSQL volume contains incompatible initialization data, recreate the local stack:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  down -v
```

Then start again.

---

## MongoDB health check fails

Check:

```dotenv
MONGO_ROOT_USERNAME=service_orders
MONGO_ROOT_PASSWORD=mongo_dev_password
```

Inspect:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs mongodb
```

If the MongoDB data volume was created with different credentials, remove the local development volume:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  down -v
```

Then start the stack again.

---

## Services restart continuously

Check the service logs.

Auth:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs --tail=200 auth-service
```

User:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs --tail=200 user-service
```

Product:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  logs --tail=200 product-service
```

Also check:

```bash
docker compose \
  --env-file .env \
  -f docker/docker-compose.yml \
  ps
```

---

## Port is already in use

The application expects:

```text
5432
6379
8025
8081
8082
8083
18081
19092
19644
27017
```

Find the process/container occupying the port and stop it, or change the corresponding host port in the Compose configuration.

---

## PowerShell cannot execute `dev-up.ps1`

If PowerShell blocks script execution, check the current policy:

```powershell
Get-ExecutionPolicy
```

For a user-level development environment, the policy can be adjusted according to the organization's security requirements.

Alternatively, Git Bash can be used:

```bash
./scripts/dev-up.sh
```

---

## Gradle build fails

Check Java:

```bash
java -version
```

The project requires Java 21.

Then try:

```bash
./gradlew clean build -x test
```

Windows:

```powershell
.\gradlew.bat clean build -x test
```

---

# Project structure

```text
test/
│
├── .github/
│   └── workflows/
│
├── gradle/
│   └── wrapper/
│
├── build-logic/
│
├── common/
│   ├── common-core/
│   ├── common-events/
│   ├── common-grpc/
│   ├── common-security/
│   └── common-test/
│
├── services/
│   ├── api-gateway/
│   ├── auth-service/
│   ├── user-service/
│   ├── product-service/
│   ├── inventory-service/
│   ├── order-service/
│   └── notification-service/
│
├── docker/
│   ├── docker-compose.yml
│   ├── Dockerfile
│   ├── init/
│   └── postgres/
│
├── scripts/
│   ├── dev-up.sh
│   └── dev-up.ps1
│
├── .env.example
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
└── README.md
```

The repository structure contains modules for the planned microservice architecture, while the current Docker development stack starts only the services described in the [Current project status](#current-project-status) section.

---

# Development

The project uses Gradle Kotlin DSL.

Build without tests:

```bash
./gradlew clean build -x test
```

Windows:

```powershell
.\gradlew.bat clean build -x test
```

Run a specific service locally when its infrastructure dependencies are available:

```bash
./gradlew :services:auth-service:bootRun
```

```bash
./gradlew :services:user-service:bootRun
```

```bash
./gradlew :services:product-service:bootRun
```

For normal local development, Docker Compose through `scripts/dev-up.sh` or `scripts/dev-up.ps1` is recommended because it starts the required infrastructure together with the application services.

---

# License

This project is an educational project.