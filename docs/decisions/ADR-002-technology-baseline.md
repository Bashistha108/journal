# ADR-002: Technology Baseline

## Context
A consistent technology stack must be pinned before development begins to avoid "it works on my machine" issues and ensure stable, reproducible builds.

## Decision
The exact technologies and versions are pinned as follows:
- **Backend:** Java 21 LTS (or newer), Spring Boot (current stable, e.g. 3.2+), Gradle with Groovy DSL.
- **Data Access:** Spring JDBC (`NamedParameterJdbcTemplate`). No JPA or Hibernate to prevent ORM interference with dynamic user schemas.
- **Database:** PostgreSQL 18. This version must be used identically across local development, tests (via Testcontainers), and production setup.
- **Migrations:** Liquibase with YAML changelogs (used exclusively for the `tj_meta` schema, never `tj_data`).
- **Frontend:** React, TypeScript (strict mode), Vite, React Router.
- **UI:** No third-party component library. Custom components with plain CSS (dark theme).

## Consequences
- Requires developers to have Java 21, Node, and Docker (for Testcontainers) installed.
- Any major version bumps must be recorded in a new ADR.
