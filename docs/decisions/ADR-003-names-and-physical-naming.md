# ADR-003: Display Names vs. Generated Physical Names

## Context
Users define table and column names dynamically. If these user-defined strings are used directly as SQL identifiers, it creates massive risks of SQL injection, reserved keyword collisions, and encoding issues.

## Decision
Users type **display names**, and the system generates **physical names** from numeric registry IDs. The two are completely decoupled.
- User table: `tj_data.t_<tableId>`
- Companion table: `tj_data.t_<tableId>_att`
- User column: `c_<columnId>`
- Metadata lives in `tj_meta`.

## Consequences
- **Positive:** Zero risk of SQL injection or reserved keyword conflicts from user input. Renaming (if implemented later) would only require updating the `display_name` in the registry, avoiding expensive `ALTER TABLE` statements.
- **Negative:** Direct database querying by humans is harder because tables look like `t_123` instead of `Trades`. This is acceptable because the app is designed not to expose direct database administration to users.
