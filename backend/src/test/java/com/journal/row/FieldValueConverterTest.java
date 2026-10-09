package com.journal.row;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

class FieldValueConverterTest {

    @Test
    void testTextConversion() {
        assertEquals("hello", FieldValueConverter.convertToJdbc("hello", "TEXT"));
        assertNull(FieldValueConverter.convertToJdbc("", "TEXT"));
        assertNull(FieldValueConverter.convertToJdbc(null, "TEXT"));
        assertEquals("hello", FieldValueConverter.convertToResponse("hello", "TEXT"));
    }

    @Test
    void testIntegerConversion() {
        assertEquals(123, FieldValueConverter.convertToJdbc(123, "INTEGER"));
        assertEquals(123, FieldValueConverter.convertToJdbc(123.0, "INTEGER"));
        assertNull(FieldValueConverter.convertToJdbc(123.5, "INTEGER"));
        assertNull(FieldValueConverter.convertToJdbc("123", "INTEGER")); // strings rejected
        assertEquals(123, FieldValueConverter.convertToResponse(123, "INTEGER"));
    }

    @Test
    void testDecimalConversion() {
        assertEquals(new BigDecimal("12.5"), FieldValueConverter.convertToJdbc("12.5", "DECIMAL"));
        assertNull(FieldValueConverter.convertToJdbc(12.5, "DECIMAL")); // must be string
        assertNull(FieldValueConverter.convertToJdbc("abc", "DECIMAL"));
        
        // Response strips trailing zeros
        assertEquals("12.5", FieldValueConverter.convertToResponse(new BigDecimal("12.500"), "DECIMAL"));
        assertEquals("100", FieldValueConverter.convertToResponse(new BigDecimal("100.00"), "DECIMAL"));
        assertEquals("-0.000001", FieldValueConverter.convertToResponse(new BigDecimal("-0.0000010"), "DECIMAL"));
    }

    @Test
    void testBooleanConversion() {
        assertEquals(true, FieldValueConverter.convertToJdbc(true, "BOOLEAN"));
        assertNull(FieldValueConverter.convertToJdbc("true", "BOOLEAN")); // strings rejected
        assertEquals(true, FieldValueConverter.convertToResponse(true, "BOOLEAN"));
    }

    @Test
    void testDateConversion() {
        assertEquals(LocalDate.of(2023, 10, 10), FieldValueConverter.convertToJdbc("2023-10-10", "DATE"));
        assertNull(FieldValueConverter.convertToJdbc("2023-13-10", "DATE"));
        assertNull(FieldValueConverter.convertToJdbc("0000-01-01", "DATE"));
        assertNull(FieldValueConverter.convertToJdbc("10000-01-01", "DATE"));
        
        assertEquals("2023-10-10", FieldValueConverter.convertToResponse(LocalDate.of(2023, 10, 10), "DATE"));
        assertEquals("2023-10-10", FieldValueConverter.convertToResponse(java.sql.Date.valueOf("2023-10-10"), "DATE"));
    }

    @Test
    void testDateTimeConversion() {
        LocalDateTime expected = LocalDateTime.of(2023, 10, 10, 15, 30, 0);
        assertEquals(expected, FieldValueConverter.convertToJdbc("2023-10-10T15:30:00", "DATETIME"));
        assertNull(FieldValueConverter.convertToJdbc("2023-10-10 15:30:00", "DATETIME"));
        
        assertEquals("2023-10-10T15:30:00", FieldValueConverter.convertToResponse(expected, "DATETIME"));
    }

    @Test
    void testSelectConversion() {
        assertEquals("Option A", FieldValueConverter.convertToJdbc("Option A", "SELECT"));
        assertEquals("Option A", FieldValueConverter.convertToResponse("Option A", "SELECT"));
    }

    @Test
    void testLinkConversion() {
        assertEquals("http://example.com", FieldValueConverter.convertToJdbc("  http://example.com  ", "LINK"));
        assertEquals("http://example.com", FieldValueConverter.convertToResponse("http://example.com", "LINK"));
    }
}
