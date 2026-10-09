package com.journal.table;

import com.journal.common.ApiError.FieldError;
import com.journal.common.ValidationException;
import com.journal.common.ResourceNotFoundException;
import com.journal.common.ConflictException;
import com.journal.metadata.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SelectOptionService {
    private final TableRegistryRepository tableRepo;
    private final ColumnRegistryRepository columnRepo;
    private final SelectOptionRepository selectRepo;
    private final JdbcTemplate jdbcTemplate;

    public SelectOptionService(TableRegistryRepository tableRepo, ColumnRegistryRepository columnRepo, SelectOptionRepository selectRepo, JdbcTemplate jdbcTemplate) {
        this.tableRepo = tableRepo;
        this.columnRepo = columnRepo;
        this.selectRepo = selectRepo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void addOptions(Long tableId, Long columnId, AddSelectOptionsRequest request) {
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(1001)");

        TableRegistry tr = tableRepo.findById(tableId).orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found"));
        ColumnRegistry cr = columnRepo.findById(columnId).orElseThrow(() -> new ResourceNotFoundException("COLUMN_NOT_FOUND", "Column not found"));
        
        if (!cr.tableId().equals(tableId)) {
            throw new ResourceNotFoundException("COLUMN_NOT_FOUND", "Column does not belong to table");
        }
        
        if (!"SELECT".equals(cr.dataType())) {
            throw new ValidationException("Invalid column", List.of(new FieldError("values", "NOT_ALLOWED", "Not a SELECT column")));
        }

        if (request.values() == null || request.values().isEmpty()) {
            throw new ValidationException("Validation failed", List.of(new FieldError("values", "REQUIRED", "Must provide values")));
        }

        List<SelectOption> existingOptions = selectRepo.findByColumnId(columnId);
        int position = existingOptions.size();
        
        List<String> newNormalized = new ArrayList<>();
        List<FieldError> errors = new ArrayList<>();

        for (int i = 0; i < request.values().size(); i++) {
            String val = request.values().get(i);
            DisplayNameValidator.validate(val, "values[" + i + "]", errors);
            if (!errors.isEmpty()) continue;
            
            String norm = DisplayNameNormalizer.normalize(val);
            if (newNormalized.contains(norm) || existingOptions.stream().anyMatch(o -> o.normalizedValue().equals(norm))) {
                errors.add(new FieldError("values[" + i + "]", "DUPLICATE_NAME", "Option already exists"));
            }
            newNormalized.add(norm);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        for (String val : request.values()) {
            String norm = DisplayNameNormalizer.normalize(val);
            selectRepo.save(new SelectOption(null, columnId, val, norm, position++));
        }
    }

    @Transactional
    public void deleteOption(Long tableId, Long columnId, Long optionId) {
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(1001)");
        
        TableRegistry tr = tableRepo.findById(tableId).orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found"));
        ColumnRegistry cr = columnRepo.findById(columnId).orElseThrow(() -> new ResourceNotFoundException("COLUMN_NOT_FOUND", "Column not found"));
        
        if (!cr.tableId().equals(tableId)) {
            throw new ResourceNotFoundException("COLUMN_NOT_FOUND", "Column does not belong to table");
        }
        
        List<SelectOption> existingOptions = selectRepo.findByColumnId(columnId);
        if (existingOptions.stream().noneMatch(o -> o.optionId().equals(optionId))) {
            throw new ResourceNotFoundException("OPTION_NOT_FOUND", "Option not found");
        }

        if (existingOptions.size() == 1) {
            throw new ValidationException("Validation failed", List.of(new FieldError("optionId", "NOT_ALLOWED", "Cannot delete the last option")));
        }
        
        String physicalTable = tr.physicalName();
        String physicalCol = cr.physicalName();
        
        Integer count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM tj_data." + physicalTable + " WHERE " + physicalCol + " = ?", 
                Integer.class, optionId
        );
        
        if (count != null && count > 0) {
            throw new ConflictException("SELECT_VALUE_IN_USE", "Option is in use by " + count + " rows");
        }

        jdbcTemplate.update("DELETE FROM tj_meta.select_option WHERE option_id = ?", optionId);
    }
}
