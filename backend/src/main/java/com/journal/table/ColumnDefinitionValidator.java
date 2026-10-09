package com.journal.table;

import com.journal.common.ApiError.FieldError;
import java.util.List;
import java.util.Set;

public class ColumnDefinitionValidator {
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "TEXT", "INTEGER", "DECIMAL", "BOOLEAN", "DATE", "DATETIME", "SELECT", "LINK", "IMAGE"
    );

    public static void validate(String name, String dataType, List<String> selectOptions, String fieldPrefix, List<FieldError> errors) {
        DisplayNameValidator.validate(name, fieldPrefix + ".name", errors);
        
        if (dataType == null || !ALLOWED_TYPES.contains(dataType)) {
            errors.add(new FieldError(fieldPrefix + ".dataType", "INVALID_TYPE", "Invalid or missing data type"));
            return;
        }

        if ("SELECT".equals(dataType)) {
            if (selectOptions == null || selectOptions.isEmpty()) {
                errors.add(new FieldError(fieldPrefix + ".selectOptions", "REQUIRED", "Select columns require at least one option"));
            } else {
                for (int i = 0; i < selectOptions.size(); i++) {
                    DisplayNameValidator.validate(selectOptions.get(i), fieldPrefix + ".selectOptions[" + i + "]", errors);
                }
            }
        } else {
            if (selectOptions != null && !selectOptions.isEmpty()) {
                errors.add(new FieldError(fieldPrefix + ".selectOptions", "NOT_ALLOWED", "Only Select columns can have options"));
            }
        }
    }
}
