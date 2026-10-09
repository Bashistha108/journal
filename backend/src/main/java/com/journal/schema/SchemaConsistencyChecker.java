package com.journal.schema;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaConsistencyChecker {
    private static final Logger log = LoggerFactory.getLogger(SchemaConsistencyChecker.class);
    private final JdbcTemplate jdbcTemplate;

    public SchemaConsistencyChecker(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkConsistency() {
        log.info("Running Schema Consistency Checker...");
        
        List<String> registeredTables = jdbcTemplate.query(
                "SELECT physical_name FROM tj_meta.table_registry",
                (rs, rowNum) -> rs.getString("physical_name")
        );

        for (String pt : registeredTables) {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'tj_data' AND table_name = ?",
                    Integer.class, pt
            );
            if (count == null || count == 0) {
                log.error("Consistency failure: Registry lists physical table {} but it does not exist in tj_data.", pt);
                throw new IllegalStateException("Database consistency check failed for table: " + pt);
            }
        }
        
        log.info("Schema Consistency Checker finished. All registered tables exist physically.");
    }
}
