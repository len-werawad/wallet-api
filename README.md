# Social Banking API (Backend Developer Assignment)

A modular Java Spring Boot backend that serves a **mobile banking UI** (based on `design.jpeg`) using the provided **MySQL schema + mock data**.

This repository focuses on:
- RESTful API design with validation and meaningful error handling
- SOLID & modular structure (Spring Modulith-style packages)
- Docker/Compose deployment (works with Docker Compose or Podman Compose)

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
- [Environment Variables](#environment-variables)
- [Database: Migration & Mock Data](#database-migration--mock-data)
- [API Documentation (Swagger)](#api-documentation-swagger)
- [Authentication](#authentication)
- [Main Endpoints (v1)](#main-endpoints-v1)
  - [Auth](#auth)
  - [Dashboard](#dashboard)
  - [Accounts](#accounts)
  - [App configuration](#app-configuration)
  - [Response Success Format](#response-success-format)
  - [Response Error Format](#response-error-format)
- [Testing](#testing)
- [Stress Test (k6)](#stress-test-k6)
- [Module Diagram (Spring Modulith)](#module-diagram-spring-modulith)
- [Improvements](#improvements)
- [Author](#author)

---

## Tech Stack

- **Java 21**
- **Spring Boot 3** (Web, Validation, Security, Actuator)
- **Spring Data JPA** + **MySQL 8**
- **Flyway** (DB migrations)
- **Redis** (caching)
- **JWT** (stateless auth)
- **OpenAPI / Swagger UI** (springdoc)
- **k6** (stress test scripts in `k6-tests/`)

---

## Project Structure

```text
social-banking-api/
├── src/
│   ├── main/
│   │   ├── java/com/lbk/socialbanking/
│   │   │   ├── WalletApplication.java
│   │   │   ├── account/
│   │   │   ├── appconfig/
│   │   │   ├── auth/
│   │   │   ├── card/
│   │   │   ├── common/
│   │   │   ├── customer/
│   │   │   ├── dashboard/
│   │   │   └── transaction/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── db/migration/
│   │           ├── V1__schema.sql
│   │           └── V2__add_indexes_and_constraints.sql
│   └── test/
│       └── java/com/lbk/socialbanking/
│           ├── ModularityTest.java
│           └── ...
├── compose.yml
├── Dockerfile
├── build.gradle
├── sql_data_dump.sql
├── k6-tests/
│   └── stress-test.js
└── docs/
    └── architecture/
        ├── components.puml
        └── components.png
```

All modules:
- `account` – account overview and related lists (accounts, goals, loans, payees)
- `appconfig` – app configuration (by environment/version/platform)
- `auth` – PIN login and token refresh (JWT)
- `card` – debit card data (design, status, details)
- `common` – shared infrastructure (security, web config, response envelope, errors)
- `customer` – customer profile and identity data
- `dashboard` – landing-page aggregation (greeting + key summaries)
- `transaction` – transaction lists and summaries (per account)

DB migrations:

- `src/main/resources/db/migration/`
  - `V1__schema.sql`
  - `V2__add_indexes_and_constraints.sql`

Performance / load testing:

- `k6-tests/stress-test.js`

---

## Getting Started

### Prerequisites

- Java 21 (for local runs)
- Docker Compose **or** Podman Compose

This project can be run in two ways:

1) **Manual (Gradle) run** – run the application locally and use containers only for dependencies.
2) **Docker/Compose run** – run everything in containers.

### Option A: Manual (Gradle) run

1. Start dependencies (MySQL + Redis):

```bash
cd /Users/werawad/Documents/lbk/git/social-banking-api
podman compose up -d
# or: docker compose up -d
```

2. Run the API locally:

```bash
./gradlew bootRun
```

API will be available at:
- `http://localhost:8080`

### Option B: Docker/Compose run

1. Uncomment the `app:` service in `compose.yml` (it is currently commented out).

2. Start the full stack:

```bash
cd /Users/werawad/Documents/lbk/git/social-banking-api
podman compose up -d --build
# or: docker compose up -d --build
```

API will be available at:
- `http://localhost:8080`

> Tip: When running in Compose, the application must connect to MySQL via the service hostname `mysql` (not `localhost`).

---

## Environment Variables

Spring reads configuration from `src/main/resources/application.yml`.

| Name | Default value | Description |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring profile to activate. |
| `SERVER_PORT` | `8080` | HTTP port for the API server. |
| `LOG_LEVEL` | `ERROR` | Root logging level (`application-dev.yml` overrides to `INFO`). |
| `DB_URL` | `jdbc:mysql://localhost:3306/social_banking_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | JDBC URL for MySQL. |
| `DB_USERNAME` | `app` | MySQL username used by the application. |
| `DB_PASSWORD` | `app` | MySQL password used by the application. |
| `DB_POOL_SIZE` | `20` | HikariCP max pool size. |
| `DB_POOL_MIN_IDLE` | `5` | HikariCP minimum idle connections. |
| `DB_CONNECTION_TIMEOUT` | `30000` | HikariCP connection timeout (ms). |
| `DB_IDLE_TIMEOUT` | `600000` | HikariCP idle timeout (ms). |
| `DB_MAX_LIFETIME` | `1800000` | HikariCP max lifetime (ms). |
| `DB_LEAK_DETECTION` | `60000` | HikariCP leak detection threshold (ms). |
| `FLYWAY_ENABLED` | `true` | Enable Flyway migrations on startup. |
| `REDIS_HOST` | `localhost` | Redis host for cache/token support. |
| `REDIS_PORT` | `6379` | Redis port. |
| `REDIS_TIMEOUT` | `2000ms` | Redis client timeout. |
| `JWT_SECRET` | `my-super-secret-jwt-key-32-bytes!!` | JWT signing secret (set a secure value in production). |
| `JWT_ISSUER` | `wallet-api` | JWT issuer claim (`iss`). |
| `JWT_ACCESS_TOKEN_EXPIRATION` | `900000` | Access token expiration (ms). |
| `JWT_REFRESH_TOKEN_EXPIRATION` | `604800000` | Refresh token expiration (ms). |

---

## Database: Migration & Mock Data

### Schema migrations (Flyway)

When the API starts with `FLYWAY_ENABLED=true`, Flyway runs migrations in:

- `classpath:db/migration`

### Mock data

This repository also includes a SQL dump file:

- `sql_data_dump.sql`

You can load it into the running MySQL container.

Example (Docker):

```bash
podman compose exec -T mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < sql_data_dump.sql
```

Example (Podman):

```bash
podman compose exec -T mysql mysql -u"$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < sql_data_dump.sql
```

> Note: If your shell doesn’t have `MYSQL_USER/MYSQL_PASSWORD/MYSQL_DATABASE` exported, set them explicitly (or create a `.env`).

---

## API Documentation (Swagger)

Once the API is running:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## Authentication

JWT Bearer auth is enabled for most `/v1/**` endpoints.

Public endpoints:
- `POST /v1/auth/login/pin`
- `POST /v1/auth/refresh`
- `GET /v1/apps/config`
- `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/**`

---

## Main Endpoints (v1)

### Auth

- `POST /v1/auth/login/pin`
- `POST /v1/auth/refresh`

### Dashboard

- `GET /v1/dashboards` (requires Bearer token)

### Accounts

- `GET /v1/accounts` (pagination: `page`, `limit`)
- `GET /v1/accounts/{accountId}/transactions` (cursor pagination: `cursor`, `limit`)
- `GET /v1/accounts/goals` (pagination: `page`, `limit`)
- `GET /v1/accounts/loans` (pagination: `page`, `limit`)
- `GET /v1/accounts/payees` (pagination: `page`, `limit`)

### App configuration

- `GET /v1/apps/config?environment=...&appVersion=...&platform=...`

For detailed schemas, use Swagger UI.


### Response Success Format
All list endpoints return paginated responses:

```json
{
  "data": [],
  "pagination": {
    "page": 1,
    "limit": 20,
    "total": 100,
    "totalPages": 5
  }
}
```

### Response Error Format
All error responses follow this structure:

Including the status field in the error response body improves clarity and reliability, especially when HTTP headers are unavailable or lost. As stated in RFC 9457 name-status, the status is advisory and helps consumers interpret the error even outside the HTTP context.

```json
{
  "error": {
    "status": 401,
    "code": "INVALID_USER",
    "message": "USER ID is incorrect.",
    "traceId": "abcd1234efgh5678"
  }
}
```

---

## Testing

Run unit tests:

```bash
./gradlew test
```

---

## Stress Test (k6)

A k6 script is provided at:

- `k6-tests/stress-test.js`

It performs:
- login (PIN)
- dashboard calls with Bearer token

Run locally (example):

```bash
k6 run k6-tests/stress-test.js
```

Update `BASE_URL` and credentials inside the script if needed.

---

## Module Diagram (Spring Modulith)

This project uses **Spring Modulith** to enforce module boundaries and generate architecture diagrams.

- **Architecture Docs:** `build/spring-modulith-docs/`

### Generate / Update

Generate/update the PlantUML sources (runs `ModularityTest#createDocumentation()`):

```bash
./gradlew test --tests com.lbk.socialbanking.ModularityTest
```

Generated sources:
- `build/spring-modulith-docs/components.puml` (overall module diagram)
- `build/spring-modulith-docs/module-*.puml` (per-module diagrams)
- `build/spring-modulith-docs/all-docs.adoc` (aggregated docs)

## Improvements

Potential follow-ups to improve security and performance:

- **JWT revocation strategy**: implement refresh-token rotation (store `jti` in Redis) and optional denylist for forced logout.
- **Use BIGINT internal primary keys**: replace `VARCHAR` PK/FK with `BIGINT` (keep UUID as `public_id` if needed) to speed up joins and reduce index size.

---
## Author
**Werawad Ruangjaroon**
- Architecture & Implementation
- Contact: [GitHub](https://github.com/len-werawad)
- Blog: [Medium](https://medium.com/@len.werawad)

---
*Built with ❤️ using Spring Boot Modular Monolith architecture*