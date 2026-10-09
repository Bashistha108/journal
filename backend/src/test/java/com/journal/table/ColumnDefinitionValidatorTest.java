package com.journal.table;

import com.journal.common.ApiError.FieldError;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColumnDefinitionValidatorTest {
    @Test
    void shouldAcceptValidTextColumn() {
        List<FieldError> errors = new ArrayList<>();
        ColumnDefinitionValidator.validate("Notes", "TEXT", null, "column", errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldAcceptValidSelectColumn() {
        List<FieldError> errors = new ArrayList<>();
        ColumnDefinitionValidator.validate("Type", "SELECT", List.of("A", "B"), "column", errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldRejectSelectWithNoOptions() {
        List<FieldError> errors = new ArrayList<>();
        ColumnDefinitionValidator.validate("Type", "SELECT", List.of(), "column", errors);
        assertEquals(1, errors.size());
        assertEquals("REQUIRED", errors.get(0).code());
    }

    @Test
    void shouldRejectTextWithOptions() {
        List<FieldError> errors = new ArrayList<>();
        ColumnDefinitionValidator.validate("Notes", "TEXT", List.of("A"), "column", errors);
        assertEquals(1, errors.size());
        assertEquals("NOT_ALLOWED", errors.get(0).code());
    }

    @Test
    void shouldRejectInvalidType() {
        List<FieldError> errors = new ArrayList<>();
        ColumnDefinitionValidator.validate("Notes", "INVALID", null, "column", errors);
        assertEquals(1, errors.size());
        assertEquals("INVALID_TYPE", errors.get(0).code());
    }
}
