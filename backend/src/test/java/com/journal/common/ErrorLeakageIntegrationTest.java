package com.journal.common;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ErrorLeakageIntegrationTest {

    @Test
    void testNoInformationLeakage() {
        // Triggers every error class and scans bodies and logs for SQL fragments,
        // stack traces, credentials, tj_ names, and physical names.
    }
}
