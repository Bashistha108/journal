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
public class ColumnRegistryRepository {
    private final JdbcTemplate jdbcTemplate;

    public ColumnRegistryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<ColumnRegistry> rowMapper = (rs, rowNum) -> new ColumnRegistry(
            rs.getLong("column_id"),
            rs.getLong("table_id"),
            rs.getInt("position"),
            rs.getString("display_name"),
            rs.getString("normalized_name"),
            rs.getString("data_type"),
            rs.getString("physical_name"),
            rs.getObject("created_at", java.time.OffsetDateTime.class)
    );

    public ColumnRegistry save(ColumnRegistry column) {
        if (column.columnId() != null) {
            jdbcTemplate.update(
                    "UPDATE tj_meta.column_registry SET table_id = ?, position = ?, display_name = ?, normalized_name = ?, data_type = ?, physical_name = ? WHERE column_id = ?",
                    column.tableId(), column.position(), column.displayName(), column.normalizedName(), column.dataType(), column.physicalName(), column.columnId()
            );
            return findById(column.columnId()).orElseThrow();
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO tj_meta.column_registry (table_id, position, display_name, normalized_name, data_type, physical_name) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, column.tableId());
            ps.setInt(2, column.position());
            ps.setString(3, column.displayName());
            ps.setString(4, column.normalizedName());
            ps.setString(5, column.dataType());
            ps.setString(6, column.physicalName());
            return ps;
        }, keyHolder);

        Long newId = ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("column_id")).longValue();
        return findById(newId).orElseThrow();
    }

    public Optional<ColumnRegistry> findById(Long columnId) {
        List<ColumnRegistry> results = jdbcTemplate.query(
                "SELECT * FROM tj_meta.column_registry WHERE column_id = ?",
                rowMapper, columnId
        );
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<ColumnRegistry> findByTableId(Long tableId) {
        return jdbcTemplate.query(
                "SELECT * FROM tj_meta.column_registry WHERE table_id = ? ORDER BY position",
                rowMapper, tableId
        );
    }
}
