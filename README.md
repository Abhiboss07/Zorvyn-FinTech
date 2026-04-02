# Zorvyn FinTech — Backend

A production-grade backend system for financial data processing with enterprise-level security: JWT + 2FA authentication, role-based access control (RBAC), AES-256-GCM field-level encryption, atomic transaction processing with compliance checks, and an immutable audit trail.

## Tech Stack
- **Java 21**
- **Spring Boot 3.3**
- **PostgreSQL 16** (via Spring Data JPA & Flyway)
- **MongoDB Atlas** (Spring Data MongoDB)
- **Redis 7** (Spring Data Redis for Sessions)
- **Docker & testcontainers**

## Prerequisites
- Java 21+
- Docker and Docker Compose
- A MongoDB Atlas connection string (since Audit Logs use MongoDB)

## Getting Started

1. **Clone the repository.**
2. **Environment Configuration:**
   Copy `.env.example` to `.env` and fill in the missing values.
   ```bash
   cp .env.example .env
   ```
   *Make sure to provide your `SPRING_DATA_MONGODB_URI`!*
3. **Run with Docker Compose (Local Dev):**
   ```bash
   docker-compose up -d
   ```
   This will start PostgreSQL and Redis containers.
4. **Build and Run the Application:**
   Generate the gradle wrapper if missing:
   ```bash
   docker run --rm -v "$PWD":/usr/src/project -w /usr/src/project gradle:8.10-jdk21 gradle wrapper
   ```
   Then run:
   ```bash
   ./gradlew bootRun
   ```

## API Documentation

Swagger UI is available at:
`http://localhost:8080/swagger-ui.html`

## Test Data

As part of Flyway migrations (`V5__seed_test_data.sql`), the database connects these test users:
- `admin@zorvyn.com`
- `manager@zorvyn.com`
- `analyst@zorvyn.com`
- `user@zorvyn.com`

All default passwords are: `Admin@123456`

## Testing

Run unit and integration tests (which utilize Testcontainers) using:
```bash
./gradlew test
```
