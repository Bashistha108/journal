package com.journal.migration;

import com.journal.support.PostgresTestContainer;
import liquibase.integration.spring.SpringLiquibase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
@Import(PostgresTestContainer.DatabaseGuardConfiguration.class)
class MigrationRerunIntegrationTest {

    @Autowired
    private SpringLiquibase liquibase;

    @Test
    void rerunShouldExecuteCleanlyAsNoOp() {
        // Liquibase already ran during context startup. We run it again to ensure it is idempotent.
        assertDoesNotThrow(() -> liquibase.afterPropertiesSet(), 
            "Rerunning Liquibase migrations should not throw any exceptions.");
    }
}
