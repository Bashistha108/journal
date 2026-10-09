# Architecture

```
User
  |
  v
Browser: React + TypeScript (Vite on 127.0.0.1:5173, proxies /api)
  |  HTTP: JSON and multipart
  v
Spring Boot REST API (127.0.0.1:8080)
  Controllers -> Services -> Repositories (Spring JDBC)
  |
  v
PostgreSQL 18 (127.0.0.1:5432)
  schema tj_meta : registries and Select values (owned by Liquibase)
  schema tj_data : user tables t_<id> and t_<id>_att (owned by runtime DDL)

Tests: JUnit + Testcontainers start a disposable PostgreSQL, apply the same
Liquibase migrations as a real install, and exercise the API against it.
```

**Frontend (React + TypeScript)** displays the three pages; keeps forms, filters, sort, and pagination state; generates controls from column definitions; shows backend validation errors; and never connects to PostgreSQL.

**Backend (Java + Spring Boot)** exposes the REST API; validates every input independently of the frontend; coordinates table creation, schema changes, rows, filtering, sorting, and attachments; owns transaction boundaries; and guarantees that only generated names reach SQL identifiers.

**PostgreSQL** stores application metadata, user tables and their records, and image bytes with their metadata, and enforces relational constraints and data types.

**Liquibase** creates and upgrades only application-owned objects in `tj_meta`. It never references `tj_data`, never uses drop-first behavior, and a test scans every changelog to prove it.

**Test environment** is an isolated, disposable PostgreSQL container with the same migrations and the same major version as a real installation. Tests and the E2E runner refuse to start against any database that is not a managed disposable instance.
