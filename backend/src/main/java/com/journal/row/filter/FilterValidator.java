package com.journal.row.filter;

import com.journal.common.ApiError.FieldError;
import com.journal.common.ValidationException;
import com.journal.metadata.ColumnRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FilterValidator {

    public static void validate(QueryRequest request, List<ColumnRegistry> columns) {
        List<FieldError> errors = new ArrayList<>();
        Map<Long, ColumnRegistry> colMap = columns.stream()
                .collect(Collectors.toMap(ColumnRegistry::columnId, c -> c));

        if (request.getFilters() != null) {
            if (request.getFilters().size() > 20) {
                errors.add(new FieldError("filters", "LIMIT_EXCEEDED", "Cannot exceed 20 filters"));
            }

            for (int i = 0; i < request.getFilters().size(); i++) {
                FilterRequest filter = request.getFilters().get(i);
                String path = "filters[" + i + "]";
                
                if (filter.getColumnId() == null) {
                    errors.add(new FieldError(path + ".columnId", "REQUIRED", "Column ID is required"));
                    continue;
                }
                if (filter.getOperator() == null) {
                    errors.add(new FieldError(path + ".operator", "REQUIRED", "Operator is required"));
                    continue;
                }
                
                ColumnRegistry col = colMap.get(filter.getColumnId());
                if (col == null) {
                    errors.add(new FieldError(path + ".columnId", "NOT_FOUND", "Column not found"));
                    continue;
                }

                validateOperator(filter, col.dataType(), path, errors);
            }
        }

        if (request.getSort() != null) {
            if (request.getSort().getColumnId() != null) {
                if (!colMap.containsKey(request.getSort().getColumnId())) {
                    errors.add(new FieldError("sort.columnId", "NOT_FOUND", "Sort column not found"));
                }
            }
            if (request.getSort().getDirection() != null) {
                String dir = request.getSort().getDirection();
                if (!"ASC".equals(dir) && !"DESC".equals(dir)) {
                    errors.add(new FieldError("sort.direction", "INVALID", "Direction must be ASC or DESC"));
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation failed", errors);
        }
    }

    private static void validateOperator(FilterRequest filter, String dataType, String path, List<FieldError> errors) {
        FilterOperator op = filter.getOperator();
        
        if (op == FilterOperator.IS_EMPTY || op == FilterOperator.IS_NOT_EMPTY) {
            return;
        }

        switch (dataType) {
            case "TEXT":
            case "LINK":
                if (op != FilterOperator.CONTAINS && op != FilterOperator.EQUALS) {
                    errors.add(new FieldError(path + ".operator", "INVALID_OPERATOR", "Operator not supported for this type"));
                }
                if (op == FilterOperator.CONTAINS || op == FilterOperator.EQUALS) {
                    if (filter.getValue() == null) {
                        errors.add(new FieldError(path + ".value", "REQUIRED", "Value is required"));
                    }
                }
                break;
            case "INTEGER":
            case "DECIMAL":
                if (op != FilterOperator.EQUALS && op != FilterOperator.GREATER_THAN && op != FilterOperator.LESS_THAN && op != FilterOperator.BETWEEN) {
                    errors.add(new FieldError(path + ".operator", "INVALID_OPERATOR", "Operator not supported for this type"));
                }
                if (op == FilterOperator.EQUALS || op == FilterOperator.GREATER_THAN || op == FilterOperator.LESS_THAN) {
                    if (filter.getValue() == null) {
                        errors.add(new FieldError(path + ".value", "REQUIRED", "Value is required"));
                    }
                }
                if (op == FilterOperator.BETWEEN) {
                    if (filter.getFrom() == null || filter.getTo() == null) {
                        errors.add(new FieldError(path, "REQUIRED", "from and to are required for BETWEEN"));
                    }
                }
                break;
            case "DATE":
            case "DATETIME":
                if (op != FilterOperator.ON && op != FilterOperator.BEFORE && op != FilterOperator.AFTER && op != FilterOperator.BETWEEN) {
                    errors.add(new FieldError(path + ".operator", "INVALID_OPERATOR", "Operator not supported for this type"));
                }
                if (op == FilterOperator.ON || op == FilterOperator.BEFORE || op == FilterOperator.AFTER) {
                    if (filter.getValue() == null) {
                        errors.add(new FieldError(path + ".value", "REQUIRED", "Value is required"));
                    }
                }
                if (op == FilterOperator.BETWEEN) {
                    if (filter.getFrom() == null || filter.getTo() == null) {
                        errors.add(new FieldError(path, "REQUIRED", "from and to are required for BETWEEN"));
                    }
                }
                break;
            case "BOOLEAN":
                if (op != FilterOperator.IS_TRUE && op != FilterOperator.IS_FALSE) {
                    errors.add(new FieldError(path + ".operator", "INVALID_OPERATOR", "Operator not supported for this type"));
                }
                break;
            case "SELECT":
                if (op != FilterOperator.EQUALS && op != FilterOperator.MATCHES_ANY) {
                    errors.add(new FieldError(path + ".operator", "INVALID_OPERATOR", "Operator not supported for this type"));
                }
                if (op == FilterOperator.EQUALS) {
                    if (filter.getValue() == null) {
                        errors.add(new FieldError(path + ".value", "REQUIRED", "Value is required"));
                    }
                }
                if (op == FilterOperator.MATCHES_ANY) {
                    if (filter.getValues() == null || filter.getValues().isEmpty()) {
                        errors.add(new FieldError(path + ".values", "REQUIRED", "Values are required"));
                    }
                }
                break;
            case "IMAGE":
                if (op != FilterOperator.HAS_ATTACHMENTS && op != FilterOperator.HAS_NO_ATTACHMENTS) {
                    errors.add(new FieldError(path + ".operator", "INVALID_OPERATOR", "Operator not supported for this type"));
                }
                break;
        }
    }
}
