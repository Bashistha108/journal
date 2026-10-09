#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

mkdir -p .run
echo "Starting backend..."

# Load env file if exists
if [ -f .env ]; then
    export $(grep -v '^#' .env | xargs)
fi

cd backend
./gradlew bootRun > ../.run/backend.log 2>&1 &
PID=$!
echo $PID > ../.run/backend.pid

# Wait briefly to check if it crashed immediately
sleep 3
if ! kill -0 $PID 2>/dev/null; then
    echo "ERROR: Backend failed to start. Last 10 lines of .run/backend.log:"
    tail -n 10 ../.run/backend.log
    exit 1
fi

echo "Backend started successfully with PID $PID. Logs at .run/backend.log"
