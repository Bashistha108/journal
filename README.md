# Trading Journal

A local-first application for journaling trades with isolated dynamic schema capabilities.

## Prerequisites

To run and build this application, you must have the following installed:
- **Java 21 LTS** (or newer)
- **Node.js 20+**
- **PostgreSQL 18** (or Docker, to run the provided `docker-compose.yml`)
- **Docker** (Required strictly for backend integration testing via Testcontainers)

## Getting Started

1. **Environment Setup:**
   Copy `.env.example` to `.env` and adjust variables if needed.

2. **Start the Application:**
   Run the backend and frontend scripts:
   ```bash
   ./scripts/start-backend.sh
   ./scripts/start-frontend.sh
   ```

3. **Stop the Application:**
   ```bash
   ./scripts/stop-application.sh
   ```

4. **Run Tests:**
   ```bash
   ./scripts/test-backend.sh
   ./scripts/test-frontend.sh
   ```

## Documentation
- Product Rules: `docs/product-specification.md`
- Database & Architecture: `docs/architecture.md`, `docs/database-design.md`
- API Contracts: `contracts/openapi.yaml`
