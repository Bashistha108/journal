package com.journal.schema;

import com.journal.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
@Import(PostgresTestContainer.DatabaseGuardConfiguration.class)
@Transactional
class SchemaConsistencyCheckerIntegrationTest {

    @Autowired
    private SchemaConsistencyChecker checker;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldPassIfConsistent() {
        assertDoesNotThrow(() -> checker.checkConsistency());
    }

    @Test
    void shouldThrowIfInconsistent() {
        jdbcTemplate.execute("INSERT INTO tj_meta.table_registry (display_name, normalized_name, physical_name) VALUES ('Phantom', 'phantom', 't_ghost')");
        
        assertThrows(IllegalStateException.class, () -> checker.checkConsistency());
    }
}
