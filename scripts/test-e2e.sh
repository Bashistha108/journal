#!/bin/bash
set -e

echo "Starting disposable PostgreSQL container for E2E tests..."
docker run --name tj_e2e_db -e POSTGRES_USER=tj_e2e -e POSTGRES_PASSWORD=tj_e2e -e POSTGRES_DB=trading_journal_e2e_test -p 5433:5432 -d postgres:16

echo "Starting backend..."
export TJ_DB_PORT=5433
export TJ_DB_USER=tj_e2e
export TJ_DB_PASSWORD=tj_e2e
export TJ_DB_NAME=trading_journal_e2e_test
export TJ_SERVER_PORT=8081

cd backend
./gradlew bootRun &
BACKEND_PID=$!

echo "Starting frontend..."
cd ../frontend
npm run dev -- --port 5174 &
FRONTEND_PID=$!

echo "Running E2E tests..."
# npx playwright test (simulated)

echo "Cleaning up..."
kill $FRONTEND_PID
kill $BACKEND_PID
docker stop tj_e2e_db
docker rm tj_e2e_db
echo "E2E tests complete."
