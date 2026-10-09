package com.journal.table;

import com.journal.common.ApiError.FieldError;
import com.journal.common.ValidationException;
import com.journal.metadata.*;
import com.journal.schema.DynamicSchemaService;
import com.journal.schema.PhysicalNaming;
import com.journal.schema.ReservedNamespacePolicy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TableService {
    private final TableRegistryRepository tableRepo;
    private final ColumnRegistryRepository columnRepo;
    private final SelectOptionRepository selectRepo;
    private final DynamicSchemaService dynamicSchemaService;
    private final JdbcTemplate jdbcTemplate;

    public TableService(TableRegistryRepository tableRepo, ColumnRegistryRepository columnRepo, SelectOptionRepository selectRepo, DynamicSchemaService dynamicSchemaService, JdbcTemplate jdbcTemplate) {
        this.tableRepo = tableRepo;
        this.columnRepo = columnRepo;
        this.selectRepo = selectRepo;
        this.dynamicSchemaService = dynamicSchemaService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public TableDetail createTable(CreateTableRequest request) {
        List<FieldError> errors = new ArrayList<>();
        DisplayNameValidator.validate(request.name(), "name", errors);
        try {
            ReservedNamespacePolicy.checkReservedPrefix(request.name());
        } catch (IllegalArgumentException e) {
            errors.add(new FieldError("name", "INVALID_NAME", e.getMessage()));
        }

        if (request.columns() == null || request.columns().isEmpty()) {
            errors.add(new FieldError("columns", "REQUIRED", "Table must have at least one column"));
        } else if (request.columns().size() > 50) {
            errors.add(new FieldError("columns", "LIMIT_EXCEEDED", "Table cannot exceed 50 columns"));
        } else {
            for (int i = 0; i < request.columns().size(); i++) {
                CreateColumnRequest col = request.columns().get(i);
                ColumnDefinitionValidator.validate(col.name(), col.dataType(), col.selectOptions(), "columns[" + i + "]", errors);
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        long uniqueCols = request.columns().stream().map(c -> DisplayNameNormalizer.normalize(c.name())).distinct().count();
        if (uniqueCols < request.columns().size()) {
            errors.add(new FieldError("columns", "DUPLICATE_NAME", "Column names must be unique"));
            throw new ValidationException("Validation failed", errors);
        }

        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(1001)");

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM tj_meta.table_registry", Integer.class);
        if (count != null && count >= 200) {
            throw new ValidationException("Limit exceeded", List.of(new FieldError("name", "LIMIT_EXCEEDED", "Cannot exceed 200 tables")));
        }

        String normalizedTableName = DisplayNameNormalizer.normalize(request.name());
        Integer tableCount = jdbcTemplate.queryForObject("SELECT count(*) FROM tj_meta.table_registry WHERE normalized_name = ?", Integer.class, normalizedTableName);
        if (tableCount != null && tableCount > 0) {
            throw new ValidationException("Duplicate name", List.of(new FieldError("name", "DUPLICATE_NAME", "Table name already exists")));
        }

        TableRegistry tr = new TableRegistry(null, request.name(), normalizedTableName, "t_pending", null);
        tr = tableRepo.save(tr);
        
        String physicalTableName = PhysicalNaming.table(tr.tableId());
        tr = tableRepo.save(new TableRegistry(tr.tableId(), tr.displayName(), tr.normalizedName(), physicalTableName, tr.createdAt()));

        List<String> physicalColumnNames = new ArrayList<>();
        List<String> dataTypes = new ArrayList<>();
        List<ColumnDetail> columnDetails = new ArrayList<>();

        for (int i = 0; i < request.columns().size(); i++) {
            CreateColumnRequest col = request.columns().get(i);
            String normColName = DisplayNameNormalizer.normalize(col.name());
            
            ColumnRegistry cr = new ColumnRegistry(null, tr.tableId(), i, col.name(), normColName, col.dataType(), "c_pending", null);
            cr = columnRepo.save(cr);
            
            String physicalColName = "IMAGE".equals(col.dataType()) ? null : PhysicalNaming.column(cr.columnId());
            cr = columnRepo.save(new ColumnRegistry(cr.columnId(), cr.tableId(), cr.position(), cr.displayName(), cr.normalizedName(), cr.dataType(), physicalColName, cr.createdAt()));
            
            physicalColumnNames.add(physicalColName);
            dataTypes.add(col.dataType());
            
            List<SelectOptionDto> optionDetails = new ArrayList<>();
            if ("SELECT".equals(col.dataType()) && col.selectOptions() != null) {
                List<String> uniqueOpts = col.selectOptions().stream().map(DisplayNameNormalizer::normalize).distinct().toList();
                if (uniqueOpts.size() < col.selectOptions().size()) {
                    throw new ValidationException("Duplicate options", List.of(new FieldError("columns[" + i + "].selectOptions", "DUPLICATE_NAME", "Select options must be unique")));
                }
                for (int j = 0; j < col.selectOptions().size(); j++) {
                    String optValue = col.selectOptions().get(j);
                    String optNorm = DisplayNameNormalizer.normalize(optValue);
                    SelectOption so = new SelectOption(null, cr.columnId(), optValue, optNorm, j);
                    so = selectRepo.save(so);
                    optionDetails.add(new SelectOptionDto(so.optionId(), so.value()));
                }
            }
            columnDetails.add(new ColumnDetail(cr.columnId(), cr.displayName(), cr.dataType(), optionDetails));
        }

        dynamicSchemaService.createTable(physicalTableName, physicalColumnNames, dataTypes);

        return new TableDetail(tr.tableId(), tr.displayName(), columnDetails);
    }
    
    public List<TableDetail> listTables() {
        return jdbcTemplate.query("SELECT * FROM tj_meta.table_registry ORDER BY created_at", (rs, rowNum) -> {
            Long tableId = rs.getLong("table_id");
            String displayName = rs.getString("display_name");
            List<ColumnDetail> columns = getColumns(tableId);
            return new TableDetail(tableId, displayName, columns);
        });
    }
    
    public TableDetail getTable(Long tableId) {
        return tableRepo.findById(tableId)
                .map(tr -> new TableDetail(tr.tableId(), tr.displayName(), getColumns(tr.tableId())))
                .orElseThrow(() -> new com.journal.common.ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found"));
    }

    private List<ColumnDetail> getColumns(Long tableId) {
        return columnRepo.findByTableId(tableId).stream().map(cr -> {
            List<SelectOptionDto> options = new ArrayList<>();
            if ("SELECT".equals(cr.dataType())) {
                options = selectRepo.findByColumnId(cr.columnId()).stream()
                        .map(so -> new SelectOptionDto(so.optionId(), so.value()))
                        .toList();
            }
            return new ColumnDetail(cr.columnId(), cr.displayName(), cr.dataType(), options);
        }).toList();
    }
}
