package com.journal.migration;

import com.journal.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
@Import(PostgresTestContainer.DatabaseGuardConfiguration.class)
class MigrationFreshInstallIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void schemasAndRegistryTablesShouldExist() {
        // Verify tj_meta exists
        Integer metaExists = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.schemata WHERE schema_name = 'tj_meta'", Integer.class);
        assertEquals(1, metaExists);

        // Verify tj_data exists
        Integer dataExists = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.schemata WHERE schema_name = 'tj_data'", Integer.class);
        assertEquals(1, dataExists);

        // Verify tables
        Integer tableRegistryExists = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'tj_meta' AND table_name = 'table_registry'", Integer.class);
        assertEquals(1, tableRegistryExists);

        Integer columnRegistryExists = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'tj_meta' AND table_name = 'column_registry'", Integer.class);
        assertEquals(1, columnRegistryExists);
    }
    
    @Test
    void tjDataSchemaShouldBeEmpty() {
        Integer tablesInTjData = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'tj_data'", Integer.class);
        assertEquals(0, tablesInTjData, "tj_data schema must be empty after fresh migration");
    }
}
