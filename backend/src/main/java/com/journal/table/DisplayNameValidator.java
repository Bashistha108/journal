package com.journal.table;

import com.journal.common.ApiError.FieldError;
import java.util.List;

public class DisplayNameValidator {
    public static void validate(String displayName, String fieldName, List<FieldError> errors) {
        if (displayName == null || displayName.trim().isEmpty()) {
            errors.add(new FieldError(fieldName, "REQUIRED", "Name cannot be empty"));
            return;
        }
        if (displayName.length() > 64) {
            errors.add(new FieldError(fieldName, "TOO_LONG", "Name must be 64 characters or fewer"));
        }
        String normalized = DisplayNameNormalizer.normalize(displayName);
        if (normalized.isEmpty()) {
            errors.add(new FieldError(fieldName, "INVALID_CHARS", "Name must contain at least one alphanumeric character"));
        }
    }
}
