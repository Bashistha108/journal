package com.journal.table;

import com.journal.common.ApiError.FieldError;
import com.journal.common.ValidationException;
import com.journal.metadata.ColumnRegistry;
import com.journal.metadata.ColumnRegistryRepository;
import com.journal.metadata.SelectOptionRepository;
import com.journal.metadata.TableRegistryRepository;
import com.journal.schema.DynamicSchemaService;
import com.journal.schema.PhysicalNaming;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ColumnService {
    private final TableRegistryRepository tableRepo;
    private final ColumnRegistryRepository columnRepo;
    private final SelectOptionRepository selectRepo;
    private final DynamicSchemaService dynamicSchemaService;
    private final JdbcTemplate jdbcTemplate;

    public ColumnService(TableRegistryRepository tableRepo, ColumnRegistryRepository columnRepo, SelectOptionRepository selectRepo, DynamicSchemaService dynamicSchemaService, JdbcTemplate jdbcTemplate) {
        this.tableRepo = tableRepo;
        this.columnRepo = columnRepo;
        this.selectRepo = selectRepo;
        this.dynamicSchemaService = dynamicSchemaService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public ColumnDetail addColumn(Long tableId, CreateColumnRequest request) {
        List<FieldError> errors = new ArrayList<>();
        ColumnDefinitionValidator.validate(request.name(), request.dataType(), request.selectOptions(), "column", errors);
        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(1001)");

        var tr = tableRepo.findById(tableId).orElseThrow(() -> new com.journal.common.ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found"));

        List<ColumnRegistry> existingColumns = columnRepo.findByTableId(tableId);
        if (existingColumns.size() >= 50) {
            throw new ValidationException("Limit exceeded", List.of(new FieldError("column", "LIMIT_EXCEEDED", "Cannot exceed 50 columns")));
        }

        String normColName = DisplayNameNormalizer.normalize(request.name());
        boolean nameExists = existingColumns.stream().anyMatch(c -> c.normalizedName().equals(normColName));
        if (nameExists) {
            throw new ValidationException("Duplicate name", List.of(new FieldError("column.name", "DUPLICATE_NAME", "Column name already exists")));
        }

        int position = existingColumns.size();
        ColumnRegistry cr = new ColumnRegistry(null, tableId, position, request.name(), normColName, request.dataType(), "c_pending", null);
        cr = columnRepo.save(cr);
        
        String physicalColName = "IMAGE".equals(request.dataType()) ? null : PhysicalNaming.column(cr.columnId());
        cr = columnRepo.save(new ColumnRegistry(cr.columnId(), cr.tableId(), cr.position(), cr.displayName(), cr.normalizedName(), cr.dataType(), physicalColName, cr.createdAt()));

        List<SelectOptionDto> optionDetails = new ArrayList<>();
        if ("SELECT".equals(request.dataType()) && request.selectOptions() != null) {
            List<String> uniqueOpts = request.selectOptions().stream().map(DisplayNameNormalizer::normalize).distinct().toList();
            if (uniqueOpts.size() < request.selectOptions().size()) {
                throw new ValidationException("Duplicate options", List.of(new FieldError("column.selectOptions", "DUPLICATE_NAME", "Select options must be unique")));
            }
            for (int j = 0; j < request.selectOptions().size(); j++) {
                String optValue = request.selectOptions().get(j);
                String optNorm = DisplayNameNormalizer.normalize(optValue);
                com.journal.metadata.SelectOption so = new com.journal.metadata.SelectOption(null, cr.columnId(), optValue, optNorm, j);
                so = selectRepo.save(so);
                optionDetails.add(new SelectOptionDto(so.optionId(), so.value()));
            }
        }

        dynamicSchemaService.addColumn(tr.physicalName(), physicalColName, request.dataType());

        return new ColumnDetail(cr.columnId(), cr.displayName(), cr.dataType(), optionDetails);
    }
}
