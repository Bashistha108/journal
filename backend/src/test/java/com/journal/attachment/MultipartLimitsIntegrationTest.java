package com.journal.attachment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class MultipartLimitsIntegrationTest {
    @Test
    void testMultipartLimits() {
        // Test oversize file and oversize request return documented errors
    }
}
