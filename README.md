# Trading Journal

A robust, local-first web application for managing trading journals.

## Prerequisites
- Java 21+
- Node.js 18+
- PostgreSQL 16+
- Docker (required for running integration and E2E tests)

## Installation & Configuration
1. Clone this repository.
2. Create a PostgreSQL database and role:
   ```sql
   CREATE USER trading_journal WITH PASSWORD 'trading_journal_pass';
   CREATE DATABASE trading_journal OWNER trading_journal;
   ```
3. Copy `.env.example` to `.env` and adjust variables if needed.

## Startup
To start the backend (starts on port 8080 by default):
```bash
cd backend
./gradlew bootRun
```

To start the frontend (starts on port 5173 by default):
```bash
cd frontend
npm install
npm run dev
```

## Testing
- Backend tests (requires Docker): `cd backend && ./gradlew test`
- Frontend tests: `cd frontend && npm test`
- End-to-End tests (requires Docker): `./scripts/test-e2e.sh`

## Backup and Restore
Use the provided scripts in `scripts/`:
- **Backup**: `./scripts/backup-database.sh <host> <port> <user> <dbname> <output_file>`
- **Restore**: `./scripts/restore-database.sh <host> <port> <user> <dbname> <input_file>`

## Troubleshooting
- **PostgreSQL Unreachable**: Verify the DB is running and credentials match your `.env`.
- **Port in Use**: Change `TJ_SERVER_PORT` in `.env` or kill the offending process.
- **Failed Migration**: Check Liquibase logs. Do not manually edit `databasechangelog`.
- **Schema Consistency Failure**: Ensure no manual schema changes were made. Restore from a backup if necessary.
