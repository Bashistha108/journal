package com.journal.metadata;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;

@Repository
public class SelectOptionRepository {
    private final JdbcTemplate jdbcTemplate;

    public SelectOptionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SelectOption> rowMapper = (rs, rowNum) -> new SelectOption(
            rs.getLong("option_id"),
            rs.getLong("column_id"),
            rs.getString("value"),
            rs.getString("normalized_value"),
            rs.getInt("position")
    );

    public SelectOption save(SelectOption option) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO tj_meta.select_option (column_id, value, normalized_value, position) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setLong(1, option.columnId());
            ps.setString(2, option.value());
            ps.setString(3, option.normalizedValue());
            ps.setInt(4, option.position());
            return ps;
        }, keyHolder);

        Long newId = ((Number) Objects.requireNonNull(keyHolder.getKeys()).get("option_id")).longValue();
        return jdbcTemplate.queryForObject(
                "SELECT * FROM tj_meta.select_option WHERE option_id = ?",
                rowMapper, newId
        );
    }

    public List<SelectOption> findByColumnId(Long columnId) {
        return jdbcTemplate.query(
                "SELECT * FROM tj_meta.select_option WHERE column_id = ? ORDER BY position",
                rowMapper, columnId
        );
    }
}
