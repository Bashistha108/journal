package com.journal.row;

import com.journal.common.ApiError.FieldError;
import com.journal.common.ConflictException;
import com.journal.common.ResourceNotFoundException;
import com.journal.common.ValidationException;
import com.journal.metadata.ColumnRegistry;
import com.journal.metadata.ColumnRegistryRepository;
import com.journal.metadata.SelectOption;
import com.journal.metadata.SelectOptionRepository;
import com.journal.metadata.TableRegistry;
import com.journal.metadata.TableRegistryRepository;
import com.journal.attachment.AttachmentService;
import com.journal.attachment.AttachmentChangeSet;
import com.journal.attachment.AttachmentMetadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RowService {
    private final TableRegistryRepository tableRepo;
    private final ColumnRegistryRepository columnRepo;
    private final SelectOptionRepository selectRepo;
    private final RowRepository rowRepo;
    private final AttachmentService attachmentService;

    public RowService(TableRegistryRepository tableRepo, ColumnRegistryRepository columnRepo, SelectOptionRepository selectRepo, RowRepository rowRepo, AttachmentService attachmentService) {
        this.tableRepo = tableRepo;
        this.columnRepo = columnRepo;
        this.selectRepo = selectRepo;
        this.rowRepo = rowRepo;
        this.attachmentService = attachmentService;
    }

    @Transactional
    public RowDetail createRow(Long tableId, RowSaveRequest request, AttachmentChangeSet changeSet) {
        TableRegistry tr = tableRepo.findById(tableId)
                .orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found"));

        List<ColumnRegistry> columns = columnRepo.findByTableId(tableId);
        
        List<FieldError> errors = new ArrayList<>();
        Map<Long, Object> jdbcValues = new HashMap<>();
        
        Map<Long, ColumnRegistry> colMap = columns.stream().collect(Collectors.toMap(ColumnRegistry::columnId, c -> c));

        if (request.getValues() != null) {
            for (Map.Entry<Long, Object> entry : request.getValues().entrySet()) {
                Long colId = entry.getKey();
                Object rawValue = entry.getValue();
                
                ColumnRegistry col = colMap.get(colId);
                if (col == null) {
                    errors.add(new FieldError("values[" + colId + "]", "BAD_REQUEST", "Unknown column id"));
                    continue;
                }
                
                if ("IMAGE".equals(col.dataType())) {
                    continue; // Ignored for values
                }

                List<String> allowedOptions = null;
                if ("SELECT".equals(col.dataType())) {
                    allowedOptions = selectRepo.findByColumnId(colId).stream().map(SelectOption::value).toList();
                }

                Object jdbcValue = null;
                try {
                    jdbcValue = FieldValueConverter.convertToJdbc(rawValue, col.dataType());
                } catch (Exception e) {
                    // Let the validator handle the error since it failed conversion
                }

                try {
                    RowValueValidator.validate(rawValue, jdbcValue, col, allowedOptions);
                    jdbcValues.put(colId, jdbcValue);
                } catch (ValidationException e) {
                    errors.addAll(e.getFieldErrors());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        RowDetail detail = rowRepo.insert(tableId, jdbcValues, columns);
        List<AttachmentMetadata> processedAttachments = attachmentService.processChangeSet(tableId, tr.physicalName(), detail.getId(), changeSet, List.of());
        detail.setAttachments(processedAttachments);
        return detail;
    }

    public RowDetail getRow(Long tableId, Long rowId) {
        if (tableRepo.findById(tableId).isEmpty()) {
            throw new ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found");
        }
        
        List<ColumnRegistry> columns = columnRepo.findByTableId(tableId);
        RowDetail detail = rowRepo.getById(tableId, rowId, columns);
        if (detail == null) {
            throw new ResourceNotFoundException("ROW_NOT_FOUND", "Row not found");
        }
        
        TableRegistry tr = tableRepo.findById(tableId).get();
        detail.setAttachments(attachmentService.getAttachments(tr.physicalName(), rowId));
        return detail;
    }

    public boolean exists(Long tableId, Long rowId) {
        return rowRepo.exists(tableId, rowId);
    }

    @Transactional
    public RowDetail updateRow(Long tableId, Long rowId, RowSaveRequest request, AttachmentChangeSet changeSet) {
        TableRegistry tr = tableRepo.findById(tableId).orElseThrow(() -> new ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found"));

        if (request.getExpectedVersion() == null) {
            throw new ValidationException("Missing expected version", List.of(new FieldError("expectedVersion", "BAD_REQUEST", "Expected version is required for update")));
        }

        List<ColumnRegistry> columns = columnRepo.findByTableId(tableId);
        List<FieldError> errors = new ArrayList<>();
        Map<Long, Object> jdbcUpdates = new HashMap<>();
        Map<Long, ColumnRegistry> colMap = columns.stream().collect(Collectors.toMap(ColumnRegistry::columnId, c -> c));

        if (request.getValues() != null) {
            for (Map.Entry<Long, Object> entry : request.getValues().entrySet()) {
                Long colId = entry.getKey();
                Object rawValue = entry.getValue();
                
                ColumnRegistry col = colMap.get(colId);
                if (col == null) {
                    errors.add(new FieldError("values[" + colId + "]", "BAD_REQUEST", "Unknown column id"));
                    continue;
                }
                
                if ("IMAGE".equals(col.dataType())) {
                    continue;
                }

                List<String> allowedOptions = null;
                if ("SELECT".equals(col.dataType())) {
                    allowedOptions = selectRepo.findByColumnId(colId).stream().map(SelectOption::value).toList();
                }

                Object jdbcValue = null;
                try {
                    jdbcValue = FieldValueConverter.convertToJdbc(rawValue, col.dataType());
                } catch (Exception e) {
                }

                try {
                    RowValueValidator.validate(rawValue, jdbcValue, col, allowedOptions);
                    jdbcUpdates.put(colId, jdbcValue);
                } catch (ValidationException e) {
                    errors.addAll(e.getFieldErrors());
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }

        int updated = rowRepo.update(tableId, rowId, request.getExpectedVersion(), jdbcUpdates, columns);
        if (updated == 0) {
            if (rowRepo.exists(tableId, rowId)) {
                throw new ConflictException("ROW_VERSION_CONFLICT", "Row changed since it was loaded");
            } else {
                throw new ResourceNotFoundException("ROW_NOT_FOUND", "Row not found");
            }
        }
        
        RowDetail detail = rowRepo.getById(tableId, rowId, columns);
        List<AttachmentMetadata> existingAttachments = attachmentService.getAttachments(tr.physicalName(), rowId);
        List<AttachmentMetadata> updatedAttachments = attachmentService.processChangeSet(tableId, tr.physicalName(), rowId, changeSet, existingAttachments);
        detail.setAttachments(updatedAttachments);
        return detail;
    }

    @Transactional
    public void deleteRow(Long tableId, Long rowId) {
        if (tableRepo.findById(tableId).isEmpty()) {
            throw new ResourceNotFoundException("TABLE_NOT_FOUND", "Table not found");
        }
        boolean deleted = rowRepo.delete(tableId, rowId);
        if (!deleted) {
            throw new ResourceNotFoundException("ROW_NOT_FOUND", "Row not found");
        }
    }
}
