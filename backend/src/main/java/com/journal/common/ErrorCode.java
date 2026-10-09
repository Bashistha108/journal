package com.journal.common;

public enum ErrorCode {
    BAD_REQUEST(400),
    FORBIDDEN_ORIGIN(403),
    TABLE_NOT_FOUND(404),
    COLUMN_NOT_FOUND(404),
    ROW_NOT_FOUND(404),
    OPTION_NOT_FOUND(404),
    ATTACHMENT_NOT_FOUND(404),
    DUPLICATE_NAME(409),
    SELECT_VALUE_IN_USE(409),
    ROW_VERSION_CONFLICT(409),
    DATABASE_CONFLICT(409),
    PAYLOAD_TOO_LARGE(413),
    UNSUPPORTED_MEDIA_TYPE(415),
    VALIDATION_FAILED(422),
    INVALID_NAME(422),
    LIMIT_EXCEEDED(422),
    SELECT_VALUE_INVALID(422),
    TEXT_TOO_LARGE(422),
    UNSUPPORTED_IMAGE(422),
    IMAGE_TOO_LARGE(422),
    TOO_MANY_ATTACHMENTS(422),
    DATABASE_UNAVAILABLE(503),
    INTERNAL_ERROR(500);

    private final int status;

    ErrorCode(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
