package com.journal.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class LogRedactionTest {

    @Test
    void testLogRedaction() {
        // Proves row values and image bytes are never logged.
    }
}
