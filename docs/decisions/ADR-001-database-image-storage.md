# ADR-001: Database Image Storage

## Context
The application needs to store image attachments (screenshots) associated with user records. Storing them on the filesystem introduces complexity around backups, transactional consistency (deleting a row must delete its images), and portability.

## Decision
Images will be stored in PostgreSQL as `BYTEA` inside a companion attachment table (`tj_data.t_<tableId>_att`) created alongside every user table.
- A foreign key with `ON DELETE CASCADE` ensures images are deleted when a row is deleted.
- Image bytes are saved in the same transaction as row data.
- The 10-per-row limit is enforced by the service during the transaction.

## Consequences
- **Positive:** Perfect transactional consistency. Simple backup process (a single `pg_dump` captures all schemas, data, and images). No external storage dependencies.
- **Negative:** Increased database size. Backups will be larger.
- **Mitigation:** The application enforces a strict 10 MiB limit per image, up to 10 images per row, to control bloat.
