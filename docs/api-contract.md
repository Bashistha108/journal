# API Contract

## API conventions

- Base path `/api/v1`. JSON for ordinary requests and responses; UTF-8 throughout.
- Application IDs (`tableId`, `columnId`, `rowId`, `attachmentId`, `optionId`) are numeric registry IDs. Physical table and column names are never exposed or accepted.
- Pagination is one-based `page` with a fixed page size of 50. Responses carry `page`, `pageSize`, `totalItems`, and `totalPages`.
- Every non-GET request must send the header `X-TJ-Client: web`. The backend also checks the `Host` header against `TJ_ALLOWED_HOSTS` and any `Origin` header against `TJ_ALLOWED_ORIGINS`.

## Endpoints

| Method and path | Purpose | Success |
| --- | --- | --- |
| `GET /api/v1/health` | database connectivity and version | 200 |
| `GET /api/v1/tables` | list tables with column counts | 200 |
| `POST /api/v1/tables` | create a table with its columns | 201 table detail |
| `GET /api/v1/tables/{tableId}` | table detail: columns in order, types, Select values | 200 |
| `POST /api/v1/tables/{tableId}/columns` | add a column | 201 column detail |
| `POST /api/v1/tables/{tableId}/columns/{columnId}/select-options` | add Select values | 201 |
| `DELETE /api/v1/tables/{tableId}/columns/{columnId}/select-options/{optionId}` | remove an unused value | 204 |
| `POST /api/v1/tables/{tableId}/rows/query` | filtered, sorted, paged rows | 200 page |
| `POST /api/v1/tables/{tableId}/rows` | create a row (with images) | 201 row detail |
| `GET /api/v1/tables/{tableId}/rows/{rowId}` | row detail with attachment metadata | 200 |
| `PATCH /api/v1/tables/{tableId}/rows/{rowId}` | update a row (with image changes) | 200 row detail |
| `DELETE /api/v1/tables/{tableId}/rows/{rowId}` | permanently delete a row and its attachments | 204 |
| `GET /api/v1/tables/{tableId}/rows/{rowId}/attachments/{attachmentId}/content` | image bytes | 200 binary |

## Errors

Every failure returns one `ApiError` body with `status`, `code`, `message`, `requestId`, and `fieldErrors` (a list of `field`, `code`, `message`; `field` is a column ID, `name`, or a path).

| Code | HTTP | Meaning |
| --- | --- | --- |
| `BAD_REQUEST` | 400 | malformed JSON, unknown field, wrong JSON type |
| `FORBIDDEN_ORIGIN` | 403 | failed the Host, Origin, or `X-TJ-Client` check |
| `TABLE_NOT_FOUND`, `COLUMN_NOT_FOUND`, `ROW_NOT_FOUND`, `OPTION_NOT_FOUND`, `ATTACHMENT_NOT_FOUND` | 404 | missing record |
| `DUPLICATE_NAME` | 409 | normalized name already exists |
| `SELECT_VALUE_IN_USE` | 409 | removal blocked because rows use the value |
| `ROW_VERSION_CONFLICT` | 409 | row changed since it was loaded |
| `DATABASE_CONFLICT` | 409 | other constraint or concurrency conflict |
| `PAYLOAD_TOO_LARGE` | 413 | request exceeds 160 MiB |
| `UNSUPPORTED_MEDIA_TYPE` | 415 | wrong request content type |
| `VALIDATION_FAILED` | 422 | one or more field errors |
| `INVALID_NAME` | 422 | name fails section 4.3 |
| `LIMIT_EXCEEDED` | 422 | table or column count limit |
| `SELECT_VALUE_INVALID` | 422 | value is not an allowed Select value |
| `TEXT_TOO_LARGE` | 422 | text over 1,048,576 bytes |
| `UNSUPPORTED_IMAGE`, `IMAGE_TOO_LARGE`, `TOO_MANY_ATTACHMENTS` | 422 | image rules in section 4.6 |
| `DATABASE_UNAVAILABLE` | 503 | PostgreSQL unreachable |
| `INTERNAL_ERROR` | 500 | unexpected failure |
