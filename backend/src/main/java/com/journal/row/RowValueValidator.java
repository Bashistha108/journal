package com.journal.row;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.journal.common.ValidationException;
import com.journal.common.ApiError.FieldError;
import com.journal.metadata.ColumnRegistry;

public class RowValueValidator {

    public static void validate(Object rawValue, Object convertedValue, ColumnRegistry column, List<String> allowedSelectOptions) {
        if (convertedValue == null) {
            if (rawValue != null) {
                 if ("TEXT".equals(column.dataType()) && rawValue.toString().isEmpty()) {
                     return;
                 }
                 throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Invalid value for type " + column.dataType())));
            }
            return;
        }
        
        switch (column.dataType()) {
            case "TEXT":
                String str = (String) convertedValue;
                if (str.getBytes(StandardCharsets.UTF_8).length > 1_048_576) {
                    throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "TEXT_TOO_LARGE", "Text exceeds maximum size of 1,048,576 bytes.")));
                }
                break;
                
            case "INTEGER":
                break;
                
            case "DECIMAL":
                BigDecimal bd = (BigDecimal) convertedValue;
                if (rawValue instanceof String) {
                    String rawStr = (String) rawValue;
                    if (rawStr.toUpperCase().contains("E")) {
                        throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Exponents are not allowed.")));
                    }
                }
                if (bd.scale() > 6) {
                    throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Maximum 6 fraction digits allowed.")));
                }
                if (bd.precision() - bd.scale() > 12) {
                    throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Maximum 12 integer digits allowed.")));
                }
                break;
                
            case "BOOLEAN":
            case "DATE":
            case "DATETIME":
                break;
                
            case "SELECT":
                String sel = (String) convertedValue;
                if (allowedSelectOptions != null && !allowedSelectOptions.contains(sel)) {
                    throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "SELECT_VALUE_INVALID", "Value is not an allowed Select value.")));
                }
                break;
                
            case "LINK":
                String link = (String) convertedValue;
                if (link.length() > 2048) {
                    throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Link exceeds 2,048 characters.")));
                }
                if (link.matches(".*\\s+.*")) {
                    throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Link cannot contain whitespace.")));
                }
                try {
                    URI uri = new URI(link);
                    if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                        throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Link must use http or https scheme.")));
                    }
                    if (uri.getHost() == null || uri.getHost().isEmpty()) {
                        throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Link must have a host.")));
                    }
                    if (uri.getUserInfo() != null) {
                        throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Link cannot contain credentials.")));
                    }
                } catch (URISyntaxException e) {
                    throw new ValidationException("Validation failed", List.of(new FieldError(column.columnId().toString(), "VALIDATION_FAILED", "Invalid link format.")));
                }
                break;
                
            case "IMAGE":
                break;
        }
    }
}
