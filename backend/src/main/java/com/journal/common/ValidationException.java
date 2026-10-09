package com.journal.common;

import java.util.List;

public class ValidationException extends RuntimeException {
    private final List<ApiError.FieldError> fieldErrors;

    public ValidationException(String message, List<ApiError.FieldError> fieldErrors) {
        super(message);
        this.fieldErrors = fieldErrors;
    }

    public List<ApiError.FieldError> getFieldErrors() {
        return fieldErrors;
    }
}
