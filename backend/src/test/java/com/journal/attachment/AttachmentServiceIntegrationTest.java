package com.journal.attachment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AttachmentServiceIntegrationTest {
    @Test
    void testCommitAndRollback() {
        // Test commit, rollback after an invalid image, after exceeding the limit, and after a version conflict
        // Also test concurrent saves can never exceed 10 attachments
    }
}
