# Product Specification

## 1. Product overview

The Trading Journal is a local, single-user application for recording and analyzing personally defined datasets. Its primary use is a flexible trading journal, but it is not tied to a fixed trading schema. Users create their own tables and columns, enter records, attach screenshots, and filter and sort their data.

Example (never mandatory; the application starts with no tables): a user creates a table named Trades with the columns Trade Date (Date), Symbol (Text), Market Bias (Select), Position Direction (Select), Setup Grade (Select), Entry Price (Decimal), Exit Price (Decimal), Profit/Loss (Decimal), Thesis (Text), and Trade Screenshot (Image attachment). Names keep their capitalization, spaces, and punctuation exactly as typed.

**Core design principles**

- **Flexible data model:** users define their own tables and columns.
- **Simple interface:** exactly three application pages.
- **Data preservation:** adding columns and upgrading the application never destroys existing user data.
- **Local operation:** everything runs on the user's computer and binds to localhost only.
- **Database-backed attachments:** images are stored in PostgreSQL as BYTEA.
- **No direct database administration:** users manage data only through the application interface.
- **Generated physical names:** user-typed names never become SQL identifiers.
- **Comprehensive testing:** correctness, migrations, filtering, sorting, attachments, security, and failure recovery are covered by automated tests that ship with each feature.

## 2. Scope

**2.1 What the application does**

- **Create and manage tables:** define table names, add columns, choose data types, configure allowed values for Select fields, and add new Select values later.
- **Record and maintain data:** create, edit, view, and permanently delete records through forms generated from the table's columns.
- **Find records:** combine filters, sort by a column, and page through results 50 rows at a time.
- **Preserve trading evidence:** attach screenshots to records, preview them, and manage them as part of saving a row. Image bytes reside in PostgreSQL.

**2.2 Not in the initial release:** dashboard, charts, broker integrations, live market data, automated trading, multi-user accounts, authentication, a SQL console, a relationship editor, an in-app backup manager, renaming or deleting tables, columns, or Select values, multi-select field values, and multi-column sorting. Backup and restore are supported through documented scripts, not through the UI.

**2.3 Accepted trade-off: names are permanent.** Because tables, columns, and Select values cannot be renamed or deleted, a typo is permanent. This is a deliberate simplification that keeps schema safety tractable. Mitigations: Page 1 shows a review step before saving that states names are permanent; columns can always be added later; and a mistaken table can simply be left unused. Rename and archive are candidates for a later release, not this one.

## 3. The three application pages
**UI should be clean and simple and beautiful with dark theme - no light theme needed. Dark black and no gradient. Use beautiful fonts (Inter).**

**Routes.** The application has exactly three page routes. `/` redirects to `/tables`; any unknown path redirects to `/tables` with a notice. Add/Edit forms and all confirmations are modal dialogs on Page 2, not routes.

| Page | Route |
| --- | --- |
| 1 — Table Management | `/tables` |
| 2 — Table Data | `/tables/:tableId` |
| 3 — Row Details | `/tables/:tableId/rows/:rowId` |

A compact sidebar is present on every page. It links to Page 1 and lists existing tables, each opening Page 2.

### Page 1 — Table Management

**Purpose:** define the structure of the journal.

**Interface elements**

- List of existing tables with their column counts.
- Create Table action, table name field, and a column-definition list (column name, data type, and for Select columns a values editor with optional presets).
- A review step before saving a new table, stating that names are permanent.
- For a selected table: its columns in creation order with types, an Add Column action, and a Select Values manager (add values; remove values not used by any row).

**Workflow — create a table:** enter a name, add 1 to 50 columns, choose each type, define Select values or choose a preset, review, save. The application then opens the new table on Page 2.

**Workflow — extend a table:** select a table, choose Add Column, define it, save. The page stays on Page 1, confirms the change, and offers an Open Table link. Existing rows receive null for the new field.

**Rules:** tables, columns, and Select values cannot be renamed or deleted. The page never exposes SQL, physical table or column names, internal metadata tables, or database administration.

**Result:** a new or extended table is ready for data entry and no existing record is affected.

### Page 2 — Table Data

**Purpose:** work with the records of one table. This is the primary working page.

**Interface elements**

- Selected table name and an Add Row action.
- Data grid showing all user-defined columns in creation order, with horizontal scrolling when columns exceed the screen width. An Image column shows the number of attachments for each row (for example `3 images`), not thumbnails.
- Sort indicators on every sortable column header (all columns except Image columns).
- Filter builder and active-filter chips.
- Pagination showing 50 rows per page.
- Edit, Delete, and View Details actions on every row.

**Workflow:** open a table; browse; add or edit a row in a modal form generated from the column definitions (including image add/remove controls); filter by one or more fields; sort by a column ascending or descending; open a row's details; delete a row after confirmation.

**Query state and navigation.** The current page, sort, and filters are held in the URL query string, so they survive reload and the browser Back button. Opening a table from the sidebar, from Page 1, or after creating it is a new navigation action and always starts with default state (page 1, no filters, ID-ascending order).

**Edit conflicts.** If a row was changed by another request after the form loaded, saving fails with a conflict message and the form offers to reload the latest values.

**Result:** users maintain and find records without SQL or database tools.

### Page 3 — Row Details

**Purpose:** inspect a single record and its screenshots.

**Interface elements**

- Every user-defined field as a label-value pair, in column creation order.
- Link values rendered as clickable links.
- An image gallery for each Image column and a click-to-preview image viewer.
- A Back to Table Data control.

**Workflow:** choose View Details on Page 2; inspect the values; open a screenshot in the preview; return. Back returns to Page 2 with the same page, sort, and filters when the user arrived from Page 2; otherwise it opens the table with default state.

**Result:** a complete view of the record, including its attachments.

## 4. Non-functional requirements

- **Performance:** with 100,000 rows and 20 columns in a table, a filtered and sorted page returns in under 1 second on an ordinary laptop. No user-configurable indexes exist in this release.
- **Browsers:** current versions of Chrome, Firefox
- **Accessibility:** dialogs trap focus and close with Escape; all controls are keyboard operable and labelled; status and errors are announced to assistive technology.
- **Language:** English interface only.
- **Logging:** the backend logs each request with a request ID, route, status, and duration. It never logs row values, image bytes, SQL parameter values, or credentials.
- **Resilience:** the application fails fast at startup with a clear message if PostgreSQL is unreachable or migrations fail. At run time a lost database connection yields HTTP 503 `DATABASE_UNAVAILABLE`, the UI shows a retry banner, and normal operation resumes when PostgreSQL returns.
