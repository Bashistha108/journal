package com.journal.row;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.journal.common.ValidationException;
import com.journal.metadata.ColumnRegistry;

class RowValueValidatorTest {

    private ColumnRegistry makeCol(String type) {
        return new ColumnRegistry(1L, 1L, 1, "col", "col", type, "c_1", OffsetDateTime.now());
    }

    @Test
    void testTextSizeLimit() {
        ColumnRegistry col = makeCol("TEXT");
        RowValueValidator.validate("valid", "valid", col, null);
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1_048_576; i++) {
            sb.append("a");
        }
        RowValueValidator.validate(sb.toString(), sb.toString(), col, null); // 1,048,576 bytes
        
        sb.append("a");
        ValidationException ex = assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate(sb.toString(), sb.toString(), col, null)
        );
        assertEquals("TEXT_TOO_LARGE", ex.getFieldErrors().get(0).code());
    }

    @Test
    void testDecimalValidation() {
        ColumnRegistry col = makeCol("DECIMAL");
        
        // valid
        RowValueValidator.validate("12.5", new BigDecimal("12.5"), col, null);
        RowValueValidator.validate("123456789012.123456", new BigDecimal("123456789012.123456"), col, null);
        
        // exponent
        ValidationException ex = assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("1.2E3", new BigDecimal("1200"), col, null)
        );
        assertEquals("VALIDATION_FAILED", ex.getFieldErrors().get(0).code());
        
        // too many fraction digits
        ex = assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("1.1234567", new BigDecimal("1.1234567"), col, null)
        );
        assertEquals("VALIDATION_FAILED", ex.getFieldErrors().get(0).code());
        
        // too many integer digits
        ex = assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("1234567890123", new BigDecimal("1234567890123"), col, null)
        );
        assertEquals("VALIDATION_FAILED", ex.getFieldErrors().get(0).code());
    }

    @Test
    void testLinkEdgeCases() {
        ColumnRegistry col = makeCol("LINK");
        
        RowValueValidator.validate("http://example.com", "http://example.com", col, null);
        RowValueValidator.validate("https://sub.domain.com/path?query=1", "https://sub.domain.com/path?query=1", col, null);
        
        // no host
        assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("http://", "http://", col, null)
        );
        
        // wrong scheme
        assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("ftp://example.com", "ftp://example.com", col, null)
        );
        
        // credentials
        assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("http://user:pass@example.com", "http://user:pass@example.com", col, null)
        );
        
        // whitespace
        assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("http://exam ple.com", "http://exam ple.com", col, null)
        );
    }

    @Test
    void testSelectMismatch() {
        ColumnRegistry col = makeCol("SELECT");
        List<String> options = List.of("A", "B");
        
        RowValueValidator.validate("A", "A", col, options);
        RowValueValidator.validate("B", "B", col, options);
        
        ValidationException ex = assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("C", "C", col, options)
        );
        assertEquals("SELECT_VALUE_INVALID", ex.getFieldErrors().get(0).code());
    }
    
    @Test
    void testNullsAndInvalidTypes() {
        ColumnRegistry col = makeCol("INTEGER");
        // Null is always valid
        RowValueValidator.validate(null, null, col, null);
        
        // Invalid type conversion
        ValidationException ex = assertThrows(ValidationException.class, () -> 
            RowValueValidator.validate("abc", null, col, null)
        );
        assertEquals("VALIDATION_FAILED", ex.getFieldErrors().get(0).code());
    }
}
