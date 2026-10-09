package com.journal.row.filter;

import com.journal.metadata.ColumnRegistry;
import com.journal.schema.SqlIdentifiers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class QuerySpecificationBuilder {

    public static class BuildResult {
        public String whereClause = "";
        public String orderByClause = "";
        public Map<String, Object> parameters = new HashMap<>();
    }

    public static BuildResult build(QueryRequest request, List<ColumnRegistry> columns) {
        BuildResult result = new BuildResult();
        Map<Long, ColumnRegistry> colMap = columns.stream()
                .collect(Collectors.toMap(ColumnRegistry::columnId, c -> c));

        if (request.getFilters() != null && !request.getFilters().isEmpty()) {
            List<String> conditions = new ArrayList<>();
            for (int i = 0; i < request.getFilters().size(); i++) {
                FilterRequest filter = request.getFilters().get(i);
                ColumnRegistry col = colMap.get(filter.getColumnId());
                if (col == null) continue;

                String colName = SqlIdentifiers.quote(col.physicalName());
                String paramName = "p" + i;

                if ("IMAGE".equals(col.dataType())) {
                    String attachTable = SqlIdentifiers.quote("t_" + col.tableId() + "_att");
                    if (filter.getOperator() == FilterOperator.HAS_ATTACHMENTS) {
                        conditions.add("(SELECT count(*) FROM tj_data." + attachTable + " WHERE row_id = t.id AND column_id = " + col.columnId() + ") > 0");
                    } else if (filter.getOperator() == FilterOperator.HAS_NO_ATTACHMENTS) {
                        conditions.add("(SELECT count(*) FROM tj_data." + attachTable + " WHERE row_id = t.id AND column_id = " + col.columnId() + ") = 0");
                    }
                    continue;
                }

                switch (filter.getOperator()) {
                    case EQUALS:
                        conditions.add(colName + " = :" + paramName);
                        result.parameters.put(paramName, filter.getValue());
                        break;
                    case CONTAINS:
                        conditions.add(colName + " ILIKE :" + paramName + " ESCAPE '\\'");
                        result.parameters.put(paramName, "%" + escapeLike(filter.getValue().toString()) + "%");
                        break;
                    case IS_EMPTY:
                        if ("TEXT".equals(col.dataType()) || "LINK".equals(col.dataType())) {
                            conditions.add("(" + colName + " IS NULL OR " + colName + " = '')");
                        } else {
                            conditions.add(colName + " IS NULL");
                        }
                        break;
                    case IS_NOT_EMPTY:
                        if ("TEXT".equals(col.dataType()) || "LINK".equals(col.dataType())) {
                            conditions.add("(" + colName + " IS NOT NULL AND " + colName + " != '')");
                        } else {
                            conditions.add(colName + " IS NOT NULL");
                        }
                        break;
                    case GREATER_THAN:
                    case AFTER:
                        conditions.add(colName + " > :" + paramName);
                        result.parameters.put(paramName, filter.getValue());
                        break;
                    case LESS_THAN:
                    case BEFORE:
                        conditions.add(colName + " < :" + paramName);
                        result.parameters.put(paramName, filter.getValue());
                        break;
                    case BETWEEN:
                        conditions.add(colName + " BETWEEN :" + paramName + "_from AND :" + paramName + "_to");
                        result.parameters.put(paramName + "_from", filter.getFrom());
                        result.parameters.put(paramName + "_to", filter.getTo());
                        break;
                    case ON:
                        conditions.add(colName + " = :" + paramName);
                        result.parameters.put(paramName, filter.getValue());
                        break;
                    case IS_TRUE:
                        conditions.add(colName + " = true");
                        break;
                    case IS_FALSE:
                        conditions.add(colName + " = false");
                        break;
                    case MATCHES_ANY:
                        if (filter.getValues() != null && !filter.getValues().isEmpty()) {
                            conditions.add(colName + " IN (:" + paramName + ")");
                            result.parameters.put(paramName, filter.getValues());
                        } else {
                            conditions.add("1=0");
                        }
                        break;
                    default:
                        break;
                }
            }
            if (!conditions.isEmpty()) {
                result.whereClause = "WHERE " + String.join(" AND ", conditions);
            }
        }

        if (request.getSort() != null && request.getSort().getColumnId() != null) {
            ColumnRegistry col = colMap.get(request.getSort().getColumnId());
            if (col != null) {
                String dir = request.getSort().getDirection() != null && request.getSort().getDirection().equals("DESC") ? "DESC" : "ASC";
                result.orderByClause = "ORDER BY " + SqlIdentifiers.quote(col.physicalName()) + " " + dir + " NULLS LAST, id ASC";
            } else {
                result.orderByClause = "ORDER BY id ASC";
            }
        } else {
            result.orderByClause = "ORDER BY id ASC";
        }

        return result;
    }

    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
