#!/bin/bash
set -e

# Starts a disposable PostgreSQL, populates it (simulated), backs it up, 
# creates a new disposable database, restores to it, and compares.

echo "Starting disposable PostgreSQL container for backup tests..."
docker run --name tj_backup_db -e POSTGRES_USER=tj_backup -e POSTGRES_PASSWORD=tj_backup -p 5434:5432 -d postgres:16
sleep 5 # wait for startup

docker exec tj_backup_db psql -U tj_backup -c "CREATE DATABASE tj_source;"
docker exec tj_backup_db psql -U tj_backup -c "CREATE DATABASE tj_target;"

echo "Simulating schema creation and data population in tj_source..."
docker exec tj_backup_db psql -U tj_backup -d tj_source -c "CREATE SCHEMA tj_meta; CREATE TABLE tj_meta.table_registry (id UUID PRIMARY KEY, name VARCHAR(255) NOT NULL);"
docker exec tj_backup_db psql -U tj_backup -d tj_source -c "INSERT INTO tj_meta.table_registry (id, name) VALUES (gen_random_uuid(), 'Test Table');"

echo "Running backup script..."
./backup-database.sh 127.0.0.1 5434 tj_backup tj_source /tmp/tj_backup.dump

echo "Running restore script..."
./restore-database.sh 127.0.0.1 5434 tj_backup tj_target /tmp/tj_backup.dump

echo "Comparing row counts in table_registry..."
SOURCE_COUNT=$(docker exec tj_backup_db psql -U tj_backup -d tj_source -t -c "SELECT count(*) FROM tj_meta.table_registry;" | xargs)
TARGET_COUNT=$(docker exec tj_backup_db psql -U tj_backup -d tj_target -t -c "SELECT count(*) FROM tj_meta.table_registry;" | xargs)

if [ "$SOURCE_COUNT" != "$TARGET_COUNT" ]; then
    echo "Count mismatch: source=$SOURCE_COUNT, target=$TARGET_COUNT"
    exit 1
fi

echo "Counts match. Backup and restore test passed."

echo "Cleaning up..."
docker stop tj_backup_db
docker rm tj_backup_db
rm /tmp/tj_backup.dump
