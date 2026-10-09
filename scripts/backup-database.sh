#!/bin/bash
set -e

if [ "$#" -ne 5 ]; then
    echo "Usage: $0 <host> <port> <user> <dbname> <output_file>"
    exit 1
fi

HOST=$1
PORT=$2
USER=$3
DBNAME=$4
OUTPUT_FILE=$5

if [ -f "$OUTPUT_FILE" ]; then
    echo "Error: Output file '$OUTPUT_FILE' already exists. Refusing to overwrite."
    exit 1
fi

echo "Backing up database '$DBNAME' to '$OUTPUT_FILE' in custom format..."
pg_dump -h "$HOST" -p "$PORT" -U "$USER" -F c -f "$OUTPUT_FILE" "$DBNAME"
echo "Backup complete."
