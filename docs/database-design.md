# Database Design

## Schemas
- `tj_meta`: Holds application metadata.
- `tj_data`: Holds user data and is written only by `DynamicSchemaService` and the row, filter, and attachment repositories.
The dedicated `trading_journal` role owns both schemas.

## Internal Metadata (schema `tj_meta`)

| Table | Key columns and constraints |
| --- | --- |
| `tj_meta.table_registry` | `table_id` identity primary key; `display_name`; `normalized_name` UNIQUE; `physical_name` UNIQUE (`t_<table_id>`); `created_at` |
| `tj_meta.column_registry` | `column_id` identity primary key; `table_id` foreign key; `position` (UNIQUE per table); `display_name`; `normalized_name` (UNIQUE per table); `data_type` with a CHECK limited to the nine API ids; `physical_name` (`c_<column_id>`, null for IMAGE); `created_at`; UNIQUE (`table_id`, `column_id`) for the companion foreign key |
| `tj_meta.select_option` | `option_id` identity primary key; `column_id` foreign key; `value`; `normalized_value` (UNIQUE per column); `position` |

## User Table `tj_data.t_<tableId>`

Created dynamically via DDL.
- `id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY`
- `row_version BIGINT NOT NULL DEFAULT 1`
- One nullable column `c_<columnId>` per non-Image column with the matching type.
- TEXT columns carry a CHECK on `octet_length <= 1048576`.
- LINK columns carry a CHECK on `char_length <= 2048`.

## Companion Attachment Table `tj_data.t_<tableId>_att`

Created together with every user table, even before any Image column exists.

| Column | Definition |
| --- | --- |
| `id` | identity primary key |
| `table_id` | NOT NULL, CHECK equal to the owning table's id |
| `row_id` | NOT NULL, foreign key to `t_<tableId>(id)` ON DELETE CASCADE, indexed |
| `column_id` | NOT NULL; composite foreign key (`table_id`, `column_id`) to `column_registry`, so the column must belong to this table |
| `original_filename` | NOT NULL, at most 255 characters, never used as a file path |
| `content_type` | NOT NULL, CHECK in `image/png`, `image/jpeg`, `image/webp` |
| `size_bytes` | NOT NULL, CHECK between 1 and 10,485,760 and equal to `octet_length(content)` |
| `content` | `BYTEA` NOT NULL |
| `created_at` | timestamp with time zone, default now |

## Atomic Schema Changes
PostgreSQL DDL is transactional. Creating a table or adding a column runs its DDL and its metadata inserts in one transaction, serialized by an advisory lock. The registry row is inserted first so the generated physical name exists.
