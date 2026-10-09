package com.journal.row;

import com.journal.metadata.ColumnRegistry;
import com.journal.schema.PhysicalNaming;
import com.journal.schema.SqlIdentifiers;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RowRepository {
    private final NamedParameterJdbcTemplate jdbc;

    public RowRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public RowDetail insert(Long tableId, Map<Long, Object> jdbcValues, List<ColumnRegistry> columns) {
        String physicalTable = PhysicalNaming.table(tableId);
        String safeTableName = SqlIdentifiers.quote("tj_data") + "." + SqlIdentifiers.quote(physicalTable);

        List<String> colNames = new ArrayList<>();
        List<String> placeholders = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();

        for (ColumnRegistry col : columns) {
            if ("IMAGE".equals(col.dataType())) continue;
            if (jdbcValues.containsKey(col.columnId())) {
                String safeCol = SqlIdentifiers.quote(col.physicalName());
                colNames.add(safeCol);
                placeholders.add(":" + col.physicalName());
                params.addValue(col.physicalName(), jdbcValues.get(col.columnId()));
            }
        }

        String sql;
        if (colNames.isEmpty()) {
            sql = "INSERT INTO " + safeTableName + " DEFAULT VALUES RETURNING id, row_version";
        } else {
            sql = "INSERT INTO " + safeTableName + " (" + String.join(", ", colNames) + ") VALUES (" + String.join(", ", placeholders) + ") RETURNING id, row_version";
        }

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id", "row_version"});

        Map<String, Object> keys = keyHolder.getKeys();
        Long id = ((Number) keys.get("id")).longValue();
        Long version = ((Number) keys.get("row_version")).longValue();

        RowDetail detail = new RowDetail();
        detail.setId(id);
        detail.setVersion(version);
        detail.setValues(new HashMap<>());
        for (ColumnRegistry col : columns) {
            if (!"IMAGE".equals(col.dataType()) && jdbcValues.containsKey(col.columnId())) {
                detail.getValues().put(col.columnId(), FieldValueConverter.convertToResponse(jdbcValues.get(col.columnId()), col.dataType()));
            }
        }
        detail.setAttachments(List.of());
        return detail;
    }

    public RowDetail getById(Long tableId, Long rowId, List<ColumnRegistry> columns) {
        String safeTableName = SqlIdentifiers.quote("tj_data") + "." + SqlIdentifiers.quote(PhysicalNaming.table(tableId));
        
        List<String> selects = new ArrayList<>();
        selects.add(SqlIdentifiers.quote("id"));
        selects.add(SqlIdentifiers.quote("row_version"));
        for (ColumnRegistry col : columns) {
            if (!"IMAGE".equals(col.dataType())) {
                selects.add(SqlIdentifiers.quote(col.physicalName()));
            }
        }
        
        String sql = "SELECT " + String.join(", ", selects) + " FROM " + safeTableName + " WHERE id = :id";
        
        List<Map<String, Object>> rows = jdbc.queryForList(sql, Map.of("id", rowId));
        if (rows.isEmpty()) {
            return null;
        }
        
        Map<String, Object> rs = rows.get(0);
        RowDetail detail = new RowDetail();
        detail.setId(((Number) rs.get("id")).longValue());
        detail.setVersion(((Number) rs.get("row_version")).longValue());
        
        Map<Long, Object> outValues = new HashMap<>();
        for (ColumnRegistry col : columns) {
            if (!"IMAGE".equals(col.dataType())) {
                Object val = rs.get(col.physicalName());
                if (val != null) {
                    outValues.put(col.columnId(), FieldValueConverter.convertToResponse(val, col.dataType()));
                } else {
                    outValues.put(col.columnId(), null);
                }
            }
        }
        detail.setValues(outValues);
        detail.setAttachments(List.of());
        return detail;
    }

    public int update(Long tableId, Long rowId, Long expectedVersion, Map<Long, Object> updates, List<ColumnRegistry> columns) {
        String safeTableName = SqlIdentifiers.quote("tj_data") + "." + SqlIdentifiers.quote(PhysicalNaming.table(tableId));
        
        List<String> setClauses = new ArrayList<>();
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", rowId);
        params.addValue("expectedVersion", expectedVersion);
        
        for (ColumnRegistry col : columns) {
            if ("IMAGE".equals(col.dataType())) continue;
            if (updates.containsKey(col.columnId())) {
                String safeCol = SqlIdentifiers.quote(col.physicalName());
                setClauses.add(safeCol + " = :" + col.physicalName());
                params.addValue(col.physicalName(), updates.get(col.columnId()));
            }
        }
        
        if (setClauses.isEmpty()) {
            String sql = "UPDATE " + safeTableName + " SET row_version = row_version + 1 WHERE id = :id AND row_version = :expectedVersion";
            return jdbc.update(sql, params);
        }
        
        setClauses.add(SqlIdentifiers.quote("row_version") + " = row_version + 1");
        String sql = "UPDATE " + safeTableName + " SET " + String.join(", ", setClauses) + " WHERE id = :id AND row_version = :expectedVersion";
        return jdbc.update(sql, params);
    }

    public boolean exists(Long tableId, Long rowId) {
        String safeTableName = SqlIdentifiers.quote("tj_data") + "." + SqlIdentifiers.quote(PhysicalNaming.table(tableId));
        Integer count = jdbc.getJdbcTemplate().queryForObject("SELECT count(1) FROM " + safeTableName + " WHERE id = ?", Integer.class, rowId);
        return count != null && count > 0;
    }

    public boolean delete(Long tableId, Long rowId) {
        String safeTableName = SqlIdentifiers.quote("tj_data") + "." + SqlIdentifiers.quote(PhysicalNaming.table(tableId));
        int rows = jdbc.getJdbcTemplate().update("DELETE FROM " + safeTableName + " WHERE id = ?", rowId);
        return rows > 0;
    }
}
