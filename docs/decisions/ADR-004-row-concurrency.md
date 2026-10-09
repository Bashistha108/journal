# ADR-004: Optimistic Row Versions

## Context
In a web application, two requests might try to edit the same row simultaneously. The last write must not blindly overwrite the first without warning.

## Decision
We use optimistic locking via a `row_version` column on every user table.
- Every user table is created with `row_version BIGINT NOT NULL DEFAULT 1`.
- When updating, the application executes: `UPDATE ... WHERE id = ? AND row_version = ?`.
- If zero rows are updated, it checks if the row exists to differentiate between `ROW_NOT_FOUND` and `ROW_VERSION_CONFLICT` (HTTP 409).
- The frontend will catch the 409 conflict and offer the user a chance to reload the latest data.

## Consequences
- Data is protected from lost updates.
- Eliminates the need for long-held database locks.
- Requires clients to explicitly send the `expectedVersion` when updating a row.
