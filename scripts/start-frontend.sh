#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

mkdir -p .run
echo "Starting frontend..."

cd frontend
npm run dev > ../.run/frontend.log 2>&1 &
PID=$!
echo $PID > ../.run/frontend.pid

# Wait briefly to check if it crashed immediately
sleep 3
if ! kill -0 $PID 2>/dev/null; then
    echo "ERROR: Frontend failed to start. Last 10 lines of .run/frontend.log:"
    tail -n 10 ../.run/frontend.log
    exit 1
fi

echo "Frontend started successfully with PID $PID. Logs at .run/frontend.log"
