# Testing Strategy

The application enforces a rigid testing model requiring each feature to ship with tests before it is considered complete.

## Categories and Expectations
1. **Unit Tests:** For isolated components (e.g., validators, normalizers, UI components without data dependencies).
2. **Integration Tests (Backend):** The backend relies exclusively on Testcontainers to spin up a disposable PostgreSQL 18 instance. All repository and service tests run against this real database. A hard guard exists (via `@PostConstruct` during tests) which refuses to run against any non-disposable database or any database whose name does not end in `_test`.
3. **UI Component Tests (Frontend):** React Testing Library tests for complex interactions (e.g., dynamic forms, filter builders).
4. **End-to-End Tests (E2E):** Playwright tests that cover complete user workflows (creating tables, saving rows with images, filtering, pagination) against fully running frontend and backend instances over test ports.
5. **Specialized Suites:**
   - `MigrationPreservationIntegrationTest`: Proves that schema upgrades do not destroy user tables, Select options, or image bytes.
   - Backup & Restore Tests: Shell scripts that populate a disposable DB, back it up via `pg_dump`, restore it via `pg_restore`, and verify identical checksums and row counts.

## Isolation
- State must not leak between tests. Database transactions should roll back after each test, or tables should be recreated.
- File system tests (e.g., image parsing) use fixtures from `src/test/resources/images/`.

## Coverage Thresholds
- Backend coverage minimum: 90%
- Frontend coverage minimum: 80%
