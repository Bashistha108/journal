# Implementation Plan

## 1. Phases

Two rules govern every phase. First, each step ships its own tests; later phases add cross-cutting suites, not the first tests of a feature. Second, a phase is done only when its exit criteria pass on a clean environment.

- **Phase 0 — Contracts, decisions, and repository conventions:** Eliminate ambiguity before implementation. Deliverable: agreed documents only; no feature code.
- **Phase 1 — Repository and development foundation:** Reproducible builds and running skeletons without journal features. Deliverable: frontend and backend start independently, configuration is externalized, and build and test commands work.
- **Phase 2 — PostgreSQL foundation, test harness, and API plumbing:** Persistent foundation, a safe test environment, and the error model. Deliverable: application connects to PostgreSQL, migrates its own schema, reports errors in one format, all integration tests run against disposable databases.
- **Phase 3 — Table management and runtime schema changes:** Implement Page 1 and services that create tables and add columns. Deliverable: users create tables, define columns, extend tables, manage Select values without losing data.
- **Phase 4 — Dynamic row CRUD and data conversion:** Create, edit, and delete rows with dynamically generated forms. Deliverable: users maintain data in any table made on Page 1 (no image handling yet).
- **Phase 5 — Filtering, sorting, and pagination:** Server-side filtering and sorting, stable pagination, and filter builder for Page 2. Deliverable: functional grid on Page 2 with performance budget met.
- **Phase 6 — PostgreSQL image attachments:** Persist images and metadata in PostgreSQL, integrate with row operations. Deliverable: complete image lifecycle with no external image directory.
- **Phase 7 — Row details and complete navigation:** Finish Page 3 and connect all three pages. Deliverable: users move between pages seamlessly with preserved state.
- **Phase 8 — Security hardening and failure recovery:** Make the application safe for local use. Deliverable: verified local-only operation, no information leakage, tested outage behavior.
- **Phase 9 — Cross-cutting automated test suites:** Verify the whole application against specification. Deliverable: repeatable contract, UI, end-to-end, upgrade, and backup tests.
- **Phase 10 — Documentation, operational readiness, and release verification:** Documented application with reproducible setup. Deliverable: release with signed-off checklist.

## 2. Dependency Map

Parallel work is allowed once its contracts are frozen (Phase 0 exit) and its prerequisite steps pass.

| Phase | Needs | May overlap with |
| --- | --- | --- |
| 0 Contracts | nothing | none |
| 1 Foundation | 0 (ADR-002 pins) | frontend and backend skeletons in parallel |
| 2 Database, harness, errors | 1 | 2.5 frontend work parallel to 2.3 and 2.4 |
| 3 Table management | 2 | 3.5 (Page 1) starts against the contract and a mock server once 2.5 exists |
| 4 Rows | 3.2 for tables to exist | 4.1 starts after Phase 2; 4.3 starts after 3.5 types exist |
| 5 Filtering | 4.2 | 5.1 and 5.3 start early; 5.4 needs 4.3 |
| 6 Attachments | 4.2 and the companion table from 3.2 | 6.1 starts after Phase 1; 6.3 needs 4.3 |
| 7 Details and navigation | 5.4 and 6.3 | 7.2 starts when the row-detail contract is frozen |
| 8 Security and recovery | 2 for 8.1; 3 to 7 for the audits | 8.1 can run alongside Phases 3 to 7 |
| 9 Cross-cutting tests | 9.1 after 5; 9.3 after 7; 9.4 and 9.5 after 6 | steps run in parallel |
| 10 Release | 9 | none |

## 3. Workstreams

| Workstream | Owns |
| --- | --- |
| W1 Contracts and documentation | Phase 0, `openapi.yaml`, examples, ADRs; reviews every contract change |
| W2 Platform backend | backend half of Phase 1; Phase 2; Phase 8 |
| W3 Schema and table backend | Phase 3.1 to 3.4 |
| W4 Row backend | Phase 4.1, 4.2; 7.1 |
| W5 Query backend | Phase 5.1, 5.2 |
| W6 Attachment backend | Phase 6.1, 6.2 |
| W7 Frontend foundation and Page 1 | frontend half of Phase 1; 2.5 frontend; 3.5; shared `components/common/` |
| W8 Frontend data entry and grid | 4.3, 4.4, 5.3, 5.4 |
| W9 Frontend attachments, Page 3, navigation | 6.3, 7.2, 7.3 |
| W10 Quality and operations | scripts, Phase 9, Phase 10, release checklist |

## 4. Shared Acceptance Checklist

| Area | Acceptance requirement | Verified by |
| --- | --- | --- |
| Table management | Create tables and add columns without losing data | `TableServiceIntegrationTest`, `ColumnServiceIntegrationTest`, E2E |
| Names | Display names preserved as typed; normalized collisions rejected; only physical names reach SQL | `DisplayNameValidator` tests, `DynamicSchemaSecurityTest`, `PhysicalNamingTest` |
| Select fields | Add values; remove only unused values; no renames; at least one value kept | `SelectOptionServiceIntegrationTest`, E2E |
| Dynamic forms | Correct control and validation for every field type | component tests, E2E |
| Row operations | Create, edit, view, and permanently delete; stale edits rejected with a conflict | `RowServiceIntegrationTest`, `RowConcurrencyIntegrationTest` |
| Text | Reject values over 1,048,576 UTF-8 bytes | `RowValueValidatorTest` |
| Decimal | Enforce NUMERIC(18,6) without rounding | `RowValueValidatorTest` |
| Filtering | Full-dataset filtering with the specified operators and logic | `RowQueryServiceIntegrationTest` |
| Sorting | Any non-image column, nulls last, stable id tie-break | `RowQueryServiceIntegrationTest` |
| Pagination | 50 per page, one-based, reset on change, 100,000-row budget met | `RowQueryPerformanceTest`, E2E |
| Navigation | Exactly three routes; Back restores page, sort, and filters; reopening resets | `AppRouter` tests, E2E |
| Links | Only http or https stored; rendered as safe anchors | `RowValueValidatorTest`, `RowDetailsView` tests |
| Images | BYTEA storage; PNG, JPEG, WebP detected from content; 10 MiB each; 10 per row | `ImageValidatorTest`, `AttachmentSchemaIntegrationTest` |
| Attachments | Cancel changes nothing; save is atomic; row deletion cascades | `AttachmentChangeSetIntegrationTest`, E2E |
| Migrations | Preserve custom tables, records, Select configuration, and image bytes | `MigrationPreservationIntegrationTest`, `ChangelogSafetyTest` |
| Security | Generated identifiers, bound parameters, localhost-only binding, Host/Origin headers checked | `NoRawIdentifierSqlTest`, `LocalOnlyFilterIntegrationTest` |
| Errors | One error format; no leaks | `GlobalExceptionHandlerTest`, `ErrorLeakageIntegrationTest` |
| Resilience | Fail-fast startup; 503 during an outage and recovery afterwards | `DatabaseOutageIntegrationTest` |
| Tests | Unit, integration, UI, end-to-end, migration, failure-recovery, and contract coverage | Phase 9 |
| Operations | Documented and verified installation, configuration, start, stop, backup, and restore | README, `test-backup-restore.sh` |
