package com.journal.row.filter;

import com.journal.common.ValidationException;
import com.journal.metadata.ColumnRegistry;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilterValidatorTest {

    private ColumnRegistry makeCol(Long id, String type) {
        return new ColumnRegistry(id, 1L, 1, "col", "col", type, "c_" + id, OffsetDateTime.now());
    }

    @Test
    void testValidTextFilter() {
        QueryRequest req = new QueryRequest();
        FilterRequest filter = new FilterRequest();
        filter.setColumnId(1L);
        filter.setOperator(FilterOperator.CONTAINS);
        filter.setValue("abc");
        req.setFilters(List.of(filter));

        assertDoesNotThrow(() -> FilterValidator.validate(req, List.of(makeCol(1L, "TEXT"))));
    }

    @Test
    void testInvalidTextFilter() {
        QueryRequest req = new QueryRequest();
        FilterRequest filter = new FilterRequest();
        filter.setColumnId(1L);
        filter.setOperator(FilterOperator.GREATER_THAN);
        filter.setValue("abc");
        req.setFilters(List.of(filter));

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            FilterValidator.validate(req, List.of(makeCol(1L, "TEXT")))
        );
        assertEquals("INVALID_OPERATOR", ex.getFieldErrors().get(0).code());
    }

    @Test
    void testValidBetweenFilter() {
        QueryRequest req = new QueryRequest();
        FilterRequest filter = new FilterRequest();
        filter.setColumnId(1L);
        filter.setOperator(FilterOperator.BETWEEN);
        filter.setFrom(10);
        filter.setTo(20);
        req.setFilters(List.of(filter));

        assertDoesNotThrow(() -> FilterValidator.validate(req, List.of(makeCol(1L, "INTEGER"))));
    }

    @Test
    void testInvalidBetweenFilter() {
        QueryRequest req = new QueryRequest();
        FilterRequest filter = new FilterRequest();
        filter.setColumnId(1L);
        filter.setOperator(FilterOperator.BETWEEN);
        filter.setFrom(10);
        // missing to
        req.setFilters(List.of(filter));

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            FilterValidator.validate(req, List.of(makeCol(1L, "INTEGER")))
        );
        assertEquals("REQUIRED", ex.getFieldErrors().get(0).code());
    }

    @Test
    void testSortValidation() {
        QueryRequest req = new QueryRequest();
        SortRequest sort = new SortRequest();
        sort.setColumnId(1L);
        sort.setDirection("ASC");
        req.setSort(sort);

        assertDoesNotThrow(() -> FilterValidator.validate(req, List.of(makeCol(1L, "INTEGER"))));

        sort.setDirection("INVALID");
        ValidationException ex = assertThrows(ValidationException.class, () -> 
            FilterValidator.validate(req, List.of(makeCol(1L, "INTEGER")))
        );
        assertEquals("INVALID", ex.getFieldErrors().get(0).code());
    }

    @Test
    void testLimitExceeded() {
        QueryRequest req = new QueryRequest();
        List<FilterRequest> filters = new ArrayList<>();
        for (int i = 0; i < 21; i++) {
            FilterRequest f = new FilterRequest();
            f.setColumnId(1L);
            f.setOperator(FilterOperator.IS_EMPTY);
            filters.add(f);
        }
        req.setFilters(filters);

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            FilterValidator.validate(req, List.of(makeCol(1L, "INTEGER")))
        );
        assertEquals("LIMIT_EXCEEDED", ex.getFieldErrors().get(0).code());
    }
}
