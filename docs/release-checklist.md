# Release Checklist

Before marking the application as ready for release (Phase 10), this checklist must be completely satisfied on a clean environment.

## Table Management
- [ ] Create tables and add columns without losing data
- [ ] Display names preserved as typed; normalized collisions rejected
- [ ] Only physical generated names reach SQL execution

## Select Fields
- [ ] Add values successfully
- [ ] Remove only unused values; no renames allowed; at least one value kept

## Row Operations & Data Types
- [ ] Correct control and validation for every field type in dynamic forms
- [ ] Create, edit, view, and permanently delete rows
- [ ] Stale edits rejected with a version conflict
- [ ] Text rejects values over 1,048,576 UTF-8 bytes
- [ ] Decimal enforces NUMERIC(18,6) without rounding
- [ ] Links store only http or https and render as safe anchors

## Images & Attachments
- [ ] Images persist securely as BYTEA in PostgreSQL
- [ ] PNG, JPEG, WebP accurately detected from content, regardless of client-supplied MIME
- [ ] Max 10 MiB each, up to 10 images per row
- [ ] Row deletion cascades to delete attachments automatically
- [ ] Cancelled forms do not leave orphaned images

## Queries & Navigation
- [ ] Full-dataset filtering works with specified operators
- [ ] Sorting applies to any non-image column, nulls last, stable ID tie-break
- [ ] Pagination fixed at 50 per page, resets on filter/sort change
- [ ] Under 1-second query time for 100,000 rows
- [ ] Exactly three UI routes; browser Back button restores query state perfectly

## Security, Stability, & Operations
- [ ] Localhost-only binding enforced; Host/Origin headers checked strictly
- [ ] Uniform error format; zero information leakage in error bodies/logs
- [ ] Fail-fast startup on missing DB; graceful 503 recovery on DB outage
- [ ] Migrations preserve custom tables and records flawlessly
- [ ] Installation, configuration, start, stop, backup, and restore are documented and verified
