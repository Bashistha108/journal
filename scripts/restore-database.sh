#!/bin/bash
set -e

if [ "$#" -ne 5 ]; then
    echo "Usage: $0 <host> <port> <user> <dbname> <input_file>"
    exit 1
fi

HOST=$1
PORT=$2
USER=$3
DBNAME=$4
INPUT_FILE=$5

if [ ! -f "$INPUT_FILE" ]; then
    echo "Error: Input file '$INPUT_FILE' does not exist."
    exit 1
fi

# Check if target database is empty. We check if there are any tables in the public schema or tj_meta schema.
TABLE_COUNT=$(psql -h "$HOST" -p "$PORT" -U "$USER" -d "$DBNAME" -t -c "SELECT count(*) FROM information_schema.tables WHERE table_schema IN ('public', 'tj_meta');" | xargs)

if [ "$TABLE_COUNT" != "0" ]; then
    echo "Error: Target database '$DBNAME' is not empty (found $TABLE_COUNT tables). Refusing to restore."
    exit 1
fi

echo "Restoring database '$DBNAME' from '$INPUT_FILE'..."
pg_restore -h "$HOST" -p "$PORT" -U "$USER" -d "$DBNAME" -1 "$INPUT_FILE"
echo "Restore complete."
