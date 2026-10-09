# Release Checklist

Before marking the application as ready for release (Phase 10), this checklist must be completely satisfied on a clean environment.

## Table Management
- [x] Create tables and add columns without losing data
- [x] Display names preserved as typed; normalized collisions rejected
- [x] Only physical generated names reach SQL execution

## Select Fields
- [x] Add values successfully
- [x] Remove only unused values; no renames allowed; at least one value kept

## Row Operations & Data Types
- [x] Correct control and validation for every field type in dynamic forms
- [x] Create, edit, view, and permanently delete rows
- [x] Stale edits rejected with a version conflict
- [x] Text rejects values over 1,048,576 UTF-8 bytes
- [x] Decimal enforces NUMERIC(18,6) without rounding
- [x] Links store only http or https and render as safe anchors

## Images & Attachments
- [x] Images persist securely as BYTEA in PostgreSQL
- [x] PNG, JPEG, WebP accurately detected from content, regardless of client-supplied MIME
- [x] Max 10 MiB each, up to 10 images per row
- [x] Row deletion cascades to delete attachments automatically
- [x] Cancelled forms do not leave orphaned images

## Queries & Navigation
- [x] Full-dataset filtering works with specified operators
- [x] Sorting applies to any non-image column, nulls last, stable ID tie-break
- [x] Pagination fixed at 50 per page, resets on filter/sort change
- [x] Under 1-second query time for 100,000 rows
- [x] Exactly three UI routes; browser Back button restores query state perfectly

## Security, Stability, & Operations
- [x] Localhost-only binding enforced; Host/Origin headers checked strictly
- [x] Uniform error format; zero information leakage in error bodies/logs
- [x] Fail-fast startup on missing DB; graceful 503 recovery on DB outage
- [x] Migrations preserve custom tables and records flawlessly
- [x] Installation, configuration, start, stop, backup, and restore are documented and verified
