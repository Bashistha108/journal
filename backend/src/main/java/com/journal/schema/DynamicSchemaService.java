package com.journal.schema;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicSchemaService {
    private final JdbcTemplate jdbcTemplate;

    public DynamicSchemaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createTable(String physicalTableName, List<String> physicalColumnNames, List<String> dataTypes) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE tj_data.").append(physicalTableName).append(" (\n");
        sql.append("id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,\n");
        sql.append("row_version BIGINT NOT NULL DEFAULT 1");
        
        for (int i = 0; i < physicalColumnNames.size(); i++) {
            if (physicalColumnNames.get(i) != null) {
                sql.append(",\n").append(physicalColumnNames.get(i)).append(" ").append(postgresType(dataTypes.get(i)));
                
                if ("TEXT".equals(dataTypes.get(i))) {
                    sql.append(" CHECK (octet_length(").append(physicalColumnNames.get(i)).append(") <= 1048576)");
                } else if ("LINK".equals(dataTypes.get(i))) {
                    sql.append(" CHECK (char_length(").append(physicalColumnNames.get(i)).append(") <= 2048)");
                }
            }
        }
        sql.append("\n);");
        
        jdbcTemplate.execute(sql.toString());
        
        String attTable = physicalTableName + "_att";
        String attSql = "CREATE TABLE tj_data." + attTable + " (\n" +
                "id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,\n" +
                "table_id BIGINT NOT NULL,\n" +
                "row_id BIGINT NOT NULL REFERENCES tj_data." + physicalTableName + "(id) ON DELETE CASCADE,\n" +
                "column_id BIGINT NOT NULL,\n" +
                "original_filename VARCHAR(255) NOT NULL,\n" +
                "content_type VARCHAR(255) NOT NULL CHECK (content_type IN ('image/png', 'image/jpeg', 'image/webp')),\n" +
                "size_bytes BIGINT NOT NULL CHECK (size_bytes BETWEEN 1 AND 10485760 AND size_bytes = octet_length(content)),\n" +
                "content BYTEA NOT NULL,\n" +
                "created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP\n" +
                ");";
        jdbcTemplate.execute(attSql);
        
        jdbcTemplate.execute("CREATE INDEX idx_" + attTable + "_row_id ON tj_data." + attTable + "(row_id);");
    }
    
    public void addColumn(String physicalTableName, String physicalColumnName, String dataType) {
        if (physicalColumnName == null) return;
        
        StringBuilder sql = new StringBuilder();
        sql.append("ALTER TABLE tj_data.").append(physicalTableName).append(" ADD COLUMN ");
        sql.append(physicalColumnName).append(" ").append(postgresType(dataType));
        
        if ("TEXT".equals(dataType)) {
            sql.append(" CHECK (octet_length(").append(physicalColumnName).append(") <= 1048576)");
        } else if ("LINK".equals(dataType)) {
            sql.append(" CHECK (char_length(").append(physicalColumnName).append(") <= 2048)");
        }
        
        jdbcTemplate.execute(sql.toString());
    }

    private String postgresType(String apiType) {
        return switch (apiType) {
            case "TEXT", "LINK" -> "TEXT";
            case "INTEGER" -> "BIGINT";
            case "DECIMAL" -> "NUMERIC(18,6)";
            case "BOOLEAN" -> "BOOLEAN";
            case "DATE" -> "DATE";
            case "DATETIME" -> "TIMESTAMP WITH TIME ZONE";
            case "SELECT" -> "TEXT";
            case "IMAGE" -> null;
            default -> throw new IllegalArgumentException("Unknown type: " + apiType);
        };
    }
}
