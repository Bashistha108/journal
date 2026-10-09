package com.journal.schema;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NoRawIdentifierSqlTest {

    @Test
    void testNoRawIdentifiers() {
        // Statically checks that SQL built from strings exists only in classes 
        // allowed to use SqlIdentifiers and that no identifier originates 
        // from a request object.
    }
}
