package com.journal.row;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FieldValueConverter {

    public static Object convertToJdbc(Object value, String dataType) {
        if (value == null) {
            return null;
        }
        
        switch (dataType) {
            case "TEXT":
            case "SELECT":
                String str = value.toString();
                if ("TEXT".equals(dataType) && str.isEmpty()) {
                    return null;
                }
                return str;
                
            case "LINK":
                return value.toString().trim();
                
            case "INTEGER":
                if (value instanceof Integer) {
                    return value;
                } else if (value instanceof Number) {
                    Number num = (Number) value;
                    if (num.doubleValue() == num.intValue()) {
                        return num.intValue();
                    }
                }
                return null;
                
            case "DECIMAL":
                if (value instanceof String) {
                    try {
                        return new BigDecimal((String) value);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                return null;
                
            case "BOOLEAN":
                if (value instanceof Boolean) {
                    return value;
                }
                return null;
                
            case "DATE":
                if (value instanceof String) {
                    try {
                        LocalDate d = LocalDate.parse((String) value);
                        if (d.getYear() < 1 || d.getYear() > 9999) return null;
                        return d;
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                }
                return null;
                
            case "DATETIME":
                if (value instanceof String) {
                    try {
                        LocalDateTime dt = LocalDateTime.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                        if (dt.getYear() < 1 || dt.getYear() > 9999) return null;
                        return dt;
                    } catch (DateTimeParseException e) {
                        return null;
                    }
                }
                return null;
                
            case "IMAGE":
                return null;
                
            default:
                throw new IllegalArgumentException("Unknown data type: " + dataType);
        }
    }

    public static Object convertToResponse(Object jdbcValue, String dataType) {
        if (jdbcValue == null) {
            return null;
        }
        switch (dataType) {
            case "TEXT":
            case "SELECT":
            case "LINK":
                return jdbcValue.toString();
            case "INTEGER":
                return jdbcValue;
            case "DECIMAL":
                return new BigDecimal(jdbcValue.toString()).stripTrailingZeros().toPlainString();
            case "BOOLEAN":
                return jdbcValue;
            case "DATE":
                if (jdbcValue instanceof LocalDate) {
                    return ((LocalDate) jdbcValue).format(DateTimeFormatter.ISO_LOCAL_DATE);
                } else if (jdbcValue instanceof java.sql.Date) {
                    return ((java.sql.Date) jdbcValue).toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                }
                return jdbcValue.toString();
            case "DATETIME":
                if (jdbcValue instanceof LocalDateTime) {
                    return ((LocalDateTime) jdbcValue).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                } else if (jdbcValue instanceof java.sql.Timestamp) {
                    return ((java.sql.Timestamp) jdbcValue).toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
                return jdbcValue.toString();
            case "IMAGE":
                return null;
            default:
                return jdbcValue;
        }
    }
}
