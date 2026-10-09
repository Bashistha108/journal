#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$DIR"

stop_process() {
    local pid_file=$1
    local name=$2
    if [ -f "$pid_file" ]; then
        PID=$(cat "$pid_file")
        if kill -0 $PID 2>/dev/null; then
            echo "Stopping $name (PID: $PID)..."
            kill $PID
            wait $PID 2>/dev/null || true
            echo "$name stopped."
        else
            echo "$name is not running."
        fi
        rm "$pid_file"
    else
        echo "No PID file found for $name."
    fi
}

stop_process ".run/backend.pid" "Backend"
stop_process ".run/frontend.pid" "Frontend"

echo "Application stopped."
