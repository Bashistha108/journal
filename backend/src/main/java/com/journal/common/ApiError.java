package com.journal.common;

import java.util.List;

public record ApiError(
    int status,
    String code,
    String message,
    String requestId,
    List<FieldError> fieldErrors
) {
    public record FieldError(
        String field,
        String code,
        String message
    ) {}
}
