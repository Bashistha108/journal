package com.journal.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfiguration.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConfiguration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void verifyDatabaseConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            logger.info("Database connection successfully established.");
        } catch (Exception e) {
            logger.error("FATAL: Failed to connect to the database. Ensure PostgreSQL is running on {}:{} with correct credentials. Error: {}",
                    System.getenv().getOrDefault("TJ_DB_HOST", "127.0.0.1"),
                    System.getenv().getOrDefault("TJ_DB_PORT", "5432"),
                    e.getMessage());
            System.exit(1);
        }
    }
}
