package com.journal.table;

import com.journal.common.ValidationException;
import com.journal.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
@Import(PostgresTestContainer.DatabaseGuardConfiguration.class)
@Transactional
class ColumnServiceIntegrationTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private ColumnService columnService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldAddColumnSafely() {
        TableDetail table = tableService.createTable(new CreateTableRequest("Existing", List.of(new CreateColumnRequest("OldCol", "TEXT", null))));
        
        // Add new column
        ColumnDetail newCol = columnService.addColumn(table.id(), new CreateColumnRequest("NewCol", "INTEGER", null));
        
        assertNotNull(newCol.id());
        assertEquals("NewCol", newCol.displayName());
        assertEquals("INTEGER", newCol.dataType());

        // Verify physical addition (should not throw)
        jdbcTemplate.execute("SELECT c_1 FROM tj_data.t_" + table.id());
    }

    @Test
    void shouldRejectDuplicateColumnAddition() {
        TableDetail table = tableService.createTable(new CreateTableRequest("Existing2", List.of(new CreateColumnRequest("Col", "TEXT", null))));
        
        ValidationException ex = assertThrows(ValidationException.class, () -> 
            columnService.addColumn(table.id(), new CreateColumnRequest(" col ", "INTEGER", null))
        );
        assertTrue(ex.getFieldErrors().stream().anyMatch(e -> e.code().equals("DUPLICATE_NAME")));
    }
}
