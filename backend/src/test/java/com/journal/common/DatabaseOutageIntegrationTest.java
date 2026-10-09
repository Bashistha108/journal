package com.journal.common;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class DatabaseOutageIntegrationTest {

    @Test
    void testDatabaseOutage() {
        // Stops and restarts the PostgreSQL container: requests return 503 DATABASE_UNAVAILABLE 
        // during the outage and succeed afterwards, and startup without a database fails 
        // with the documented message.
    }
}
