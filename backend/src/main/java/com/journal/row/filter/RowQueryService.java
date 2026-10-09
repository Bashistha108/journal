package com.journal.row.filter;

import com.journal.common.ResourceNotFoundException;
import com.journal.metadata.ColumnRegistry;
import com.journal.metadata.ColumnRegistryRepository;
import com.journal.metadata.TableRegistryRepository;
import com.journal.row.RowListItem;
import com.journal.schema.SqlIdentifiers;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RowQueryService {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final TableRegistryRepository tableRepo;
    private final ColumnRegistryRepository columnRepo;

    public RowQueryService(NamedParameterJdbcTemplate jdbcTemplate, TableRegistryRepository tableRepo, ColumnRegistryRepository columnRepo) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableRepo = tableRepo;
        this.columnRepo = columnRepo;
    }

    @Transactional(readOnly = true)
    public PageResponse<RowListItem> query(Long tableId, QueryRequest request) {
        if (tableRepo.findById(tableId).isEmpty()) {
            throw new ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found");
        }

        List<ColumnRegistry> columns = columnRepo.findByTableId(tableId);
        FilterValidator.validate(request, columns);

        QuerySpecificationBuilder.BuildResult spec = QuerySpecificationBuilder.build(request, columns);

        String tableName = "tj_data." + SqlIdentifiers.quote("t_" + tableId);
        
        String countSql = "SELECT count(*) FROM " + tableName + " t " + spec.whereClause;
        Integer totalItems = jdbcTemplate.queryForObject(countSql, spec.parameters, Integer.class);
        if (totalItems == null) totalItems = 0;

        int pageSize = 50;
        int page = request.getPage() != null && request.getPage() > 0 ? request.getPage() : 1;
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        if (page > totalPages && totalPages > 0) page = totalPages;
        
        int offset = (page - 1) * pageSize;

        String querySql = "SELECT t.* FROM " + tableName + " t " + spec.whereClause + " " + spec.orderByClause + " LIMIT :limit OFFSET :offset";
        spec.parameters.put("limit", pageSize);
        spec.parameters.put("offset", offset);

        List<RowListItem> items = jdbcTemplate.query(querySql, spec.parameters, (rs, rowNum) -> mapRow(rs, columns));

        if (!items.isEmpty()) {
            List<Long> rowIds = items.stream().map(RowListItem::getId).toList();
            String attCountSql = "SELECT row_id, column_id, count(*) as cnt FROM tj_data.t_" + tableId + "_att WHERE row_id IN (:rowIds) GROUP BY row_id, column_id";
            jdbcTemplate.query(attCountSql, Map.of("rowIds", rowIds), rs -> {
                Long rId = rs.getLong("row_id");
                Long cId = rs.getLong("column_id");
                int cnt = rs.getInt("cnt");
                for (RowListItem item : items) {
                    if (item.getId().equals(rId)) {
                        item.getAttachmentCounts().put(cId, cnt);
                        break;
                    }
                }
            });
        }

        return new PageResponse<>(page, pageSize, totalItems, totalPages, items);
    }

    private RowListItem mapRow(ResultSet rs, List<ColumnRegistry> columns) throws SQLException {
        Long id = rs.getLong("id");
        Long version = rs.getLong("row_version");
        OffsetDateTime createdAt = rs.getObject("created_at", OffsetDateTime.class);
        OffsetDateTime updatedAt = rs.getObject("updated_at", OffsetDateTime.class);

        Map<Long, Object> values = new HashMap<>();
        Map<Long, Integer> attachmentCounts = new HashMap<>();
        for (ColumnRegistry col : columns) {
            String physicalName = col.physicalName();
            if (col.dataType().equals("IMAGE")) {
                attachmentCounts.put(col.columnId(), 0);
            } else {
                Object val = rs.getObject(physicalName);
                if (val != null) {
                    values.put(col.columnId(), val);
                }
            }
        }
        RowListItem item = new RowListItem();
        item.setId(id);
        item.setVersion(version);
        item.setValues(values);
        item.setAttachmentCounts(attachmentCounts);
        return item;
    }
}
