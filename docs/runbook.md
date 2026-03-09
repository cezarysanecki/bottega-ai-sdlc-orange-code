# Runbook – Healthcare Platform

This runbook provides guidance for local development and troubleshooting.

## Prerequisites
- Java 22+
- Maven 3.9+
- Node.js 18+ and npm 9+

## First-Time Setup

1. **Clone and Install Dependencies**
   ```bash
   # Install frontend dependencies
   cd frontend/med-frontend
   npm install

   # Build backend
   cd ../..
   cd backend
   ./mvnw clean install
   ```

## Running the Stack

**Terminal 1 - Backend:**
```bash
./run-backend.sh
```

**Terminal 2 - Frontend:**
```bash
./run-frontend.sh
```

Services:
- Backend: http://localhost:8080
- Frontend: http://localhost:5173

## Database Management

The backend runs with an embedded PostgreSQL instance in demo mode - no external database setup required.

## Testing

### Backend Tests
```bash
cd backend
./mvnw test              # Unit tests
./mvnw verify            # Full verification with integration tests
```

### Frontend Tests
```bash
cd frontend/med-frontend
npm test                 # Run Vitest tests
npm run lint             # ESLint check
```

## Troubleshooting

### Backend Won't Start
- Verify Java version: `java -version` should show 22.x
- Review logs for dependency injection errors

### Frontend Build Errors
- Clear node_modules and reinstall: `rm -rf node_modules && npm install`
- Check Node version: `node -v` should be 18.x or higher
- Verify backend is accessible at http://localhost:8080

### Port Conflicts
- Check for processes using required ports: `lsof -i :8080,5173`
- Stop conflicting services or modify port configuration

## Monitoring and Logs

### Health Checks
- Backend: http://localhost:8080/actuator/health

### Updating Dependencies
```bash
# Backend
cd backend && ./mvnw clean install -U

# Frontend
cd frontend/med-frontend && npm update
```

## Known Issues

### Telehealth Session Status
- Some session IDs may return `FAILED` status from mock service
- This is expected behavior for testing error handling

### Cache Configuration
- Provider schedule cache uses default settings
- Clear cache by restarting backend service

## Support

For additional support or questions:
- Review documentation in `docs/`
- Check backlog in `docs/backlog.md` for known issues
- Consult team conventions in `docs/conventions.md`
