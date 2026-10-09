package com.journal.metadata;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

@Repository
public class TableRegistryRepository {
    private final JdbcTemplate jdbcTemplate;

    public TableRegistryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<TableRegistry> rowMapper = (rs, rowNum) -> new TableRegistry(
            rs.getLong("table_id"),
            rs.getString("display_name"),
            rs.getString("normalized_name"),
            rs.getString("physical_name"),
            rs.getObject("created_at", java.time.OffsetDateTime.class)
    );

    public TableRegistry save(TableRegistry table) {
        if (table.tableId() != null) {
            jdbcTemplate.update(
                    "UPDATE tj_meta.table_registry SET display_name = ?, normalized_name = ?, physical_name = ? WHERE table_id = ?",
                    table.displayName(), table.normalizedName(), table.physicalName(), table.tableId()
            );
            return findById(table.tableId()).orElseThrow();
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO tj_meta.table_registry (display_name, normalized_name, physical_name) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, table.displayName());
            ps.setString(2, table.normalizedName());
            ps.setString(3, table.physicalName());
            return ps;
        }, keyHolder);

        Long newId = ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("table_id")).longValue();
        return findById(newId).orElseThrow();
    }

    public Optional<TableRegistry> findById(Long tableId) {
        List<TableRegistry> results = jdbcTemplate.query(
                "SELECT * FROM tj_meta.table_registry WHERE table_id = ?",
                rowMapper, tableId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
}
