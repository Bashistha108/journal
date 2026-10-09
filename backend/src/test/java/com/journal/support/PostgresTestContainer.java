package com.journal.support;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresTestContainer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    // Matches ADR-002 version
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18")
            .withDatabaseName("trading_journal_test")
            .withUsername("test_user")
            .withPassword("test_pass")
            .withReuse(true);

    static {
        postgres.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Map<String, Object> testProperties = Map.of(
                "TEST_DB_URL", postgres.getJdbcUrl(),
                "TEST_DB_USER", postgres.getUsername(),
                "TEST_DB_PASSWORD", postgres.getPassword()
        );
        applicationContext.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("testcontainers", testProperties));
    }

    /**
     * The guard that makes tests refuse any database that is not a managed disposable instance
     * or whose name does not end in `_test`.
     */
    @TestConfiguration
    public static class DatabaseGuardConfiguration {
        private final JdbcTemplate jdbcTemplate;

        public DatabaseGuardConfiguration(JdbcTemplate jdbcTemplate) {
            this.jdbcTemplate = jdbcTemplate;
        }

        @PostConstruct
        public void verifyTestDatabase() {
            try {
                String dbName = jdbcTemplate.queryForObject("SELECT current_database()", String.class);
                if (dbName == null || (!dbName.endsWith("_test") && !dbName.startsWith("testcontainers"))) {
                    throw new IllegalStateException(
                            "SECURITY VIOLATION: Test suite attempted to connect to a non-test database: " + dbName
                    );
                }
            } catch (Exception e) {
                if (e instanceof IllegalStateException) {
                    throw e;
                }
                throw new IllegalStateException("Could not verify database name in test guard.", e);
            }
        }
    }
}
