package com.journal.attachment;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class AttachmentRepository {

    private final NamedParameterJdbcTemplate jdbc;

    public AttachmentRepository(NamedParameterJdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public AttachmentMetadata insert(String physicalTableName, Long tableId, Long rowId, Long columnId, String filename, String contentType, long sizeBytes, byte[] content) {
        String sql = "INSERT INTO tj_data." + physicalTableName + "_att " +
            "(table_id, row_id, column_id, original_filename, content_type, size_bytes, content) " +
            "VALUES (:tableId, :rowId, :columnId, :filename, :contentType, :size, :content) " +
            "RETURNING id, created_at";
        
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("tableId", tableId)
                .addValue("rowId", rowId)
                .addValue("columnId", columnId)
                .addValue("filename", filename)
                .addValue("contentType", contentType)
                .addValue("size", sizeBytes)
                .addValue("content", content);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sql, params, keyHolder, new String[]{"id", "created_at"});
        
        Map<String, Object> keys = keyHolder.getKeys();
        Long id = ((Number) keys.get("id")).longValue();
        Object createdAtObj = keys.get("created_at");
        OffsetDateTime createdAt = null;
        if (createdAtObj instanceof java.sql.Timestamp) {
            createdAt = ((java.sql.Timestamp) createdAtObj).toInstant().atOffset(java.time.ZoneOffset.UTC);
        } else if (createdAtObj instanceof OffsetDateTime) {
            createdAt = (OffsetDateTime) createdAtObj;
        }
        
        AttachmentMetadata metadata = new AttachmentMetadata();
        metadata.setId(id);
        metadata.setColumnId(columnId);
        metadata.setFilename(filename);
        metadata.setContentType(contentType);
        metadata.setSizeBytes(sizeBytes);
        metadata.setCreatedAt(createdAt);
        return metadata;
    }

    public void deleteByRowIdAndIds(String physicalTableName, Long rowId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        String sql = "DELETE FROM tj_data." + physicalTableName + "_att WHERE row_id = :rowId AND id IN (:ids)";
        jdbc.update(sql, Map.of("rowId", rowId, "ids", ids));
    }

    public List<AttachmentMetadata> findByRowId(String physicalTableName, Long rowId) {
        String sql = "SELECT id, column_id, original_filename, content_type, size_bytes, created_at " +
            "FROM tj_data." + physicalTableName + "_att " +
            "WHERE row_id = :rowId ORDER BY created_at ASC";
            
        return jdbc.query(sql, Map.of("rowId", rowId), (rs, rowNum) -> {
            AttachmentMetadata m = new AttachmentMetadata();
            m.setId(rs.getLong("id"));
            m.setColumnId(rs.getLong("column_id"));
            m.setFilename(rs.getString("original_filename"));
            m.setContentType(rs.getString("content_type"));
            m.setSizeBytes(rs.getLong("size_bytes"));
            m.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            return m;
        });
    }

    public Optional<byte[]> getContent(String physicalTableName, Long rowId, Long id) {
        String sql = "SELECT content FROM tj_data." + physicalTableName + "_att WHERE row_id = :rowId AND id = :id";
        List<byte[]> res = jdbc.query(sql, Map.of("rowId", rowId, "id", id), (rs, rowNum) -> rs.getBytes("content"));
        if (res.isEmpty()) return Optional.empty();
        return Optional.of(res.get(0));
    }

    public Optional<AttachmentMetadata> findById(String physicalTableName, Long rowId, Long id) {
        String sql = "SELECT id, column_id, original_filename, content_type, size_bytes, created_at " +
            "FROM tj_data." + physicalTableName + "_att " +
            "WHERE row_id = :rowId AND id = :id";
            
        List<AttachmentMetadata> res = jdbc.query(sql, Map.of("rowId", rowId, "id", id), (rs, rowNum) -> {
            AttachmentMetadata m = new AttachmentMetadata();
            m.setId(rs.getLong("id"));
            m.setColumnId(rs.getLong("column_id"));
            m.setFilename(rs.getString("original_filename"));
            m.setContentType(rs.getString("content_type"));
            m.setSizeBytes(rs.getLong("size_bytes"));
            m.setCreatedAt(rs.getObject("created_at", OffsetDateTime.class));
            return m;
        });
        if (res.isEmpty()) return Optional.empty();
        return Optional.of(res.get(0));
    }
}
