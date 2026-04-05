# Zorvyn FinTech Ecosystem 🚀

A production-grade, full-stack financial data processing and access control system. This enterprise application is built with a rugged security-first architecture including stateless JWT/OAuth2 authentication (with TOTP 2FA), strictly partitioned Role-Based Access Control (RBAC), and AES-256 field-level data encryption.

## 🌟 Professional Summary & Project Architecture

This application was engineered to exceed modern financial compliance and security standards through a multi-tier micro-architecture:

- **Polyglot Persistence Layer:** Core immutable financial ledgers and relational user data are secured in **PostgreSQL**. High-velocity, write-heavy audit events are securely piped into **MongoDB** for horizontal scalability. Sessions and rate-limiting protocols are managed via **Redis**.
- **Financial Transaction Engine:** A rigidly transactional backend core ensuring ACID compliance. Features built-in limits, atomic concurrency overdraft protection, and deterministic state validations.
- **Enterprise Security Hardening:**
  - Zero-Trust endpoint routing configured via Spring Security 6.
  - Global exception translation shielding internal system stack traces from endpoints.
  - Network defense enabled via CSP, HSTS, and X-XSS secure headers.
  - Sensitive columns (e.g., account routing paths and 2FA secrets) encrypted at rest automatically.
- **Automated Infrastructure Verification:** Flyway migrations construct the schema layers safely and instantly inject localized datasets mapped strictly for dynamic bootstrapping protocols.

## 🛠 Tech Stack
- **Backend Core:** Java 26, Spring Boot 3.5.x
- **Frontend Core:** React, TypeScript, Vite
- **Databases:** PostgreSQL 16 (Relational), MongoDB 7 (Document), Redis 7 (Cache)
- **Infrastructure:** Docker Compose, Flyway, Bucket4j (Rate Limiting)

## 🚀 Getting Started

### 1. Start the Containerized Infrastructure
The application relies on Docker to orchestrate its background dependencies.
```bash
docker compose up -d postgres mongodb redis
```

### 2. Launch the Backend API
```bash
./gradlew bootRun
```

### 3. Launch the Frontend UI
In a separate terminal, interface with the Vite React application:
```bash
cd frontend
npm install
npm run dev
```

## 🔐 Built-In Test Users
Upon launch, Flyway provisions a robust sandbox environment. Access the portal at `http://localhost:5173` using:

| Role | Email | Password |
| :--- | :--- | :--- |
| **System Admin** | `admin@zorvyn.com` | `Admin@123456` |
| **Finance Manager**| `manager@zorvyn.com` | `Admin@123456` |
| **Data Analyst** | `analyst@zorvyn.com` | `Admin@123456` |
| **Standard User** | `user@zorvyn.com` | `Admin@123456` |

*Note: All identities are protected natively by BCrypt 12-round secure hashes.*

## 📖 API Documentation
A fully interactive OpenAPI/Swagger visualization map is available natively during active sessions:
`http://localhost:8080/swagger-ui.html`

## 🛡️ Testing & Validation
Launch the backend suite, utilizing isolated Testcontainers for emulation testing sequences:
```bash
./gradlew test
```
