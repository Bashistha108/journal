# Validation Rules

## 1. Names and Identifiers

Users type **display names**; the system generates **physical names**. The two are never derived from each other.

- **Display-name rules** (tables, columns, and Select values): 1 to 64 characters after trimming; internal runs of whitespace collapse to one space; no control characters. Tables and columns must start with a letter or digit (any Unicode letter or digit) and may contain Unicode letters, digits, marks, spaces, and these punctuation characters: `_ - / . , ( ) + % & # : '`. Select values may contain any printable characters. The display form keeps the user's capitalization.
- **Normalized key:** Unicode NFKC, trim, collapse whitespace, lowercase (root locale). Uniqueness is checked on the normalized key: table names are unique application-wide, column names unique within a table, Select values unique within a column. Collisions (for example `Symbol` and `  symbol `) are rejected.
- **Physical names** are generated from numeric registry IDs: user table `tj_data.t_<tableId>`, its companion attachment table `tj_data.t_<tableId>_att`, and user column `c_<columnId>`. Image columns have no physical column. Because no user text ever reaches SQL as an identifier, reserved-word and injection problems cannot arise from names.

## 2. Field Values

- Every user-defined column is nullable with no default. Empty form fields are stored as SQL NULL. Entirely empty rows are permitted.
- Decimals are returned as canonical plain strings with trailing zeros removed (`1.5`, `100`, `-0.000001`).
- Dates and timestamps carry no time zone anywhere in the system. The backend and JDBC session run in UTC so that no conversion can occur by accident.
- Limits enforced:
  - Tables per application: 200
  - Columns per table: 50
  - Display name length: 64 characters
  - Select values per column: 1 to 50
  - Text field: 1,048,576 UTF-8 bytes
  - Link field: 2,048 characters
  - Request size: 160 MiB

## 3. Image Attachments

- **Accepted Formats:** PNG, JPEG, WebP only. The type is detected from the file's actual bytes; the client-declared content type is ignored and the detected type is stored.
- **Size Limits:** At most 10 MiB (10,485,760 bytes) each.
- **Dimension Limits:** At most 20,000 px per side and 100 megapixels.
- **Quantity:** Each attachment belongs to one row and one Image column. A row can hold at most 10 attachments in total across all Image columns.
- **Lifecycle:** Attachments are added and removed only as part of saving the row (create or update). Deleting a row deletes its attachments in the same transaction.

## 4. Select Fields and Presets

- A Select column needs 1 to 50 values and keeps them in the configured order.
- Values can be **added** at any time. Values cannot be **renamed**.
- A value can be **removed** only if no row uses it, and the last remaining value cannot be removed. To retire a value that is in use, first edit those rows, then remove it.
- **Presets** are templates offered in the values editor; choosing one pre-fills editable values. They are not tied to column names:
  - Market Bias: Bullish, Bearish, Neutral
  - Position Direction: Long, Short
  - Setup Grade: A+, A, B+, B, C+, C, D

## 5. Filters

The operators per type are fixed:

| Type | Operators |
| --- | --- |
| Text, Link | contains, equals, is empty, is not empty |
| Integer, Decimal | equals, greater than, less than, between, is empty, is not empty |
| Date | on, before, after, between, is empty, is not empty |
| Date and time | before, after, between, is empty, is not empty |
| Boolean | is true, is false, is empty |
| Select | matches any selected value, is empty, is not empty |
| Image attachment | has attachments, has no attachments (for that Image column) |

- Greater than, less than, before, and after are strict. Between is inclusive of both ends.
- Text and Link matching is always case-insensitive. Contains escapes the characters `%`, `_`, and the backslash so users search for them literally. Select matching is exact.
- Separate filters combine with AND. Multiple values inside one Select filter combine with OR.
- Filters per query limit: 20; at most 50 values in one Select filter.
