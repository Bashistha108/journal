# Type Mapping

This document is the central mapping for all nine field types: UI label, API id, serialization, validation, storage, and display. Empty form values become null; Integer and Decimal are validated before any write; Select values match exactly; text length is measured in UTF-8 bytes.

| UI label | API id | PostgreSQL storage | JSON form | Rules and Validation | Display Format |
| --- | --- | --- | --- | --- | --- |
| Text | `TEXT` | TEXT, CHECK octet_length ≤ 1,048,576 | string | UTF-8 length at most 1,048,576 bytes; stored exactly as entered; an empty string becomes null | Exact text |
| Integer | `INTEGER` | INTEGER | number | −2,147,483,648 to 2,147,483,647; fractions, exponents, and strings are rejected | Digits without thousands separators |
| Decimal | `DECIMAL` | NUMERIC(18,6) | string such as `12.5` | at most 12 integer digits and 6 fraction digits; more fraction digits are rejected, never rounded; no exponent | Canonical plain strings with trailing zeros removed (`1.5`, `100`, `-0.000001`) |
| Boolean | `BOOLEAN` | BOOLEAN | true, false, null | no other spellings accepted | `Yes` / `No` |
| Date | `DATE` | DATE | string `YYYY-MM-DD` | a real calendar date, years 0001 to 9999 | `YYYY-MM-DD` |
| Date and time | `DATETIME` | TIMESTAMP WITHOUT TIME ZONE | string `YYYY-MM-DDTHH:mm:ss` | no zone designator or offset; second precision | `YYYY-MM-DD HH:mm:ss` (24-hour, no zone) |
| Select | `SELECT` | TEXT | string | must equal one configured allowed value exactly (case-sensitive) | Exact match |
| Link | `LINK` | TEXT, CHECK char_length ≤ 2,048 | string | trimmed; scheme http or https; host required; no embedded credentials; no whitespace | Clickable links |
| Image attachment | `IMAGE` | (companion attachment table, BYTEA) | attachment metadata | PNG, JPEG, WebP; ≤ 10 MiB; ≤ 20,000 px / 100 MP; ≤ 10 per row | Image gallery / count (e.g. `3 images`) |

Nulls are shown as an em dash (—) across all types.
