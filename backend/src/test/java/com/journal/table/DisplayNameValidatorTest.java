package com.journal.table;

import com.journal.common.ApiError.FieldError;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DisplayNameValidatorTest {
    @Test
    void shouldAcceptValidName() {
        List<FieldError> errors = new ArrayList<>();
        DisplayNameValidator.validate("Trade Date", "name", errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldRejectEmptyName() {
        List<FieldError> errors = new ArrayList<>();
        DisplayNameValidator.validate("  ", "name", errors);
        assertEquals(1, errors.size());
        assertEquals("REQUIRED", errors.get(0).code());
    }

    @Test
    void shouldRejectTooLongName() {
        List<FieldError> errors = new ArrayList<>();
        String longName = "a".repeat(65);
        DisplayNameValidator.validate(longName, "name", errors);
        assertEquals(1, errors.size());
        assertEquals("TOO_LONG", errors.get(0).code());
    }

    @Test
    void shouldRejectNamesWithNoAlphanumerics() {
        List<FieldError> errors = new ArrayList<>();
        DisplayNameValidator.validate("!!!???", "name", errors);
        assertEquals(1, errors.size());
        assertEquals("INVALID_CHARS", errors.get(0).code());
    }
}
