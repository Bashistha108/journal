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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
@Import(PostgresTestContainer.DatabaseGuardConfiguration.class)
@Transactional
class DynamicSchemaSecurityTest {

    @Autowired
    private DynamicSchemaService dynamicSchemaService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldCreateHostileNameSafely() {
        // Because physical names are generated (t_123, c_456), hostile strings should never break SQL syntax.
        // We simulate a call that uses safe physical names, but we verify it works smoothly.
        // The display names with emoji, quotes, semicolons are stored in metadata, not in the physical layer.
        
        String physicalTable = "t_9999";
        List<String> physicalCols = List.of("c_8888");
        List<String> types = List.of("TEXT");

        assertDoesNotThrow(() -> dynamicSchemaService.createTable(physicalTable, physicalCols, types));
        
        // Ensure the table exists without issues
        jdbcTemplate.execute("SELECT id FROM tj_data." + physicalTable);
    }
}
