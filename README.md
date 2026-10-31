# BankEase

BankEase is a polished banking MVP with a Java 17 / Spring Boot 3 backend and a React/Vite frontend. It demonstrates session-based authentication, role-aware workflows, a simulated double-entry-style ledger, transfers, bill payments, loans, investments, and support/admin operations.

## Project layout

- `backend/` — Spring Boot 3 REST API (Java 17, Maven)
- `frontend/` — React + TypeScript + Vite web app
- `docker-compose.yml` — MySQL 8.4 for local persistence

The backend starts in mock/in-memory mode by default so the complete demo works without a database. MySQL configuration is ready when persistence is desired.

## Quick start

### 1. Start the API

```powershell
cd .\backend
.\start-backend.ps1
```

The API is available at `http://localhost:8080`.
Verify it before opening the frontend at `http://localhost:8080/api/health`; it must return `{"status":"ok"}`.

### 2. Start the web app

```powershell
cd ..\frontend
npm install
npm run dev
```

Open `http://localhost:5173`.

### 3. Optional MySQL

From `BankEase/` (the Compose file publishes MySQL on the standard local port `3306`):

```powershell
docker compose up -d mysql
```

Set `SPRING_PROFILES_ACTIVE=mysql` before starting the backend to use the MySQL profile. The Compose database name, user, password, and port match the backend defaults. The default profile remains safe and self-contained for demos.

## Demo accounts

The seeded demo users are shown on the sign-in screen. Typical accounts are:

| Role | Username | Password |
| --- | --- | --- |
| Customer | `customer` | `customer123` |
| Admin | `admin` | `admin123` |
| Support | `support` | `support123` |

## Validation

```powershell
cd .\backend
mvn test
mvn package

cd ..\frontend
npm run build
```

## API notes

The frontend uses cookie-backed sessions (`credentials: include`). During Vite development, `/api` is proxied to `http://localhost:8080`, avoiding browser CORS issues. Set `VITE_API_URL` only when the API is hosted elsewhere.

This is an intentionally simulated banking environment: no real payment rails, credit decisions, or investment execution are performed.
