package com.journal.schema;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlIdentifiersTest {
    @Test
    void shouldQuoteIdentifiers() {
        assertEquals("\"t_123\"", SqlIdentifiers.quote("t_123"));
    }

    @Test
    void shouldEscapeQuotesInsideIdentifiers() {
        assertEquals("\"t_\"\"123\"", SqlIdentifiers.quote("t_\"123"));
    }

    @Test
    void shouldRejectNullIdentifiers() {
        assertThrows(IllegalArgumentException.class, () -> SqlIdentifiers.quote(null));
    }
}
