# Healthcare Appointment & Billing Platform

![CI Status](https://img.shields.io/badge/ci-passing-brightgreen)

A comprehensive healthcare scheduling and billing platform built with Spring Boot and React.

---

## Quick Start

Run the application with two terminals - one for backend, one for frontend with hot reload.

### Prerequisites

- **Java 22+** - Download from [Adoptium](https://adoptium.net/) (recommended) or [Oracle](https://www.oracle.com/java/technologies/downloads/)
- **Node.js 18+** - Download from [nodejs.org](https://nodejs.org/)

### Run the Application

**Terminal 1 - Backend (API server with embedded PostgreSQL):**

```bash
# Mac / Linux
./run-backend.sh

# Windows
run-backend.bat
```

**Terminal 2 - Frontend (Vite dev server with hot reload):**

```bash
# Mac / Linux
./run-frontend.sh

# Windows
run-frontend.bat
```

Then open **http://localhost:5173** in your browser.

### Service URLs
- **Frontend UI**: http://localhost:5173 (with hot reload)
- **Backend API**: http://localhost:8080

> **Note:** First backend run takes 2-5 minutes to download dependencies. Frontend `npm install` runs automatically on first start.

---

## Overview

This monorepo contains a full-stack healthcare management system supporting appointment scheduling, patient management, billing operations, and telehealth sessions.

## Repository Layout

```
backend/        Spring Boot services and domain modules
frontend/       React + TypeScript application
ops/            Operational assets (mocks, seed data)
docs/           Architecture and API documentation
```

## Testing

```bash
# Backend
cd backend && ./mvnw verify

# Frontend
cd frontend/med-frontend && npm test
```

## Architecture

### Backend (med-backend)
- **Framework**: Spring Boot 3.2.5, Java 22
- **Database**: PostgreSQL 14 with Flyway migrations
- **APIs**: REST endpoints + GraphQL + SOAP client
- **Key Libraries**: Spring Data JPA, MapStruct, Caffeine cache, JWT auth

### Frontend (med-frontend)
- **Framework**: React 18 + TypeScript
- **Build**: Vite
- **UI**: Ant Design + legacy jQuery FullCalendar
- **State**: React Query, Redux Toolkit, Context API

## Documentation
- [Architecture Overview](docs/architecture.md)
- [Development Runbook](docs/runbook.md)
- [Team Conventions](docs/conventions.md)

## Contributing

1. Create a feature branch from `main`
2. Run tests:
   - Backend: `cd backend && ./mvnw verify`
   - Frontend: `cd frontend/med-frontend && npm test`
3. Submit a pull request

## License

Proprietary - All rights reserved
