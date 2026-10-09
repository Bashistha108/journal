package com.journal.table;

import com.journal.support.PostgresTestContainer;
import com.journal.common.ValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
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
class TableServiceIntegrationTest {

    @Autowired
    private TableService tableService;

    @Test
    void shouldCreateTableSuccessfully() {
        CreateTableRequest request = new CreateTableRequest(
                "My Table \"; DROP TABLE users;",
                List.of(
                        new CreateColumnRequest("Notes \uD83D\uDE00", "TEXT", null),
                        new CreateColumnRequest("Type", "SELECT", List.of("A", "B"))
                )
        );

        TableDetail detail = tableService.createTable(request);

        assertNotNull(detail.id());
        assertEquals("My Table \"; DROP TABLE users;", detail.displayName());
        assertEquals(2, detail.columns().size());
        
        assertEquals("Notes \uD83D\uDE00", detail.columns().get(0).displayName());
        assertEquals("TEXT", detail.columns().get(0).dataType());
        
        assertEquals("Type", detail.columns().get(1).displayName());
        assertEquals("SELECT", detail.columns().get(1).dataType());
        assertEquals(2, detail.columns().get(1).selectOptions().size());
    }

    @Test
    void shouldRejectDuplicateTableName() {
        CreateTableRequest request1 = new CreateTableRequest("Trades", List.of(new CreateColumnRequest("Col", "TEXT", null)));
        tableService.createTable(request1);

        CreateTableRequest request2 = new CreateTableRequest("  TRADES  ", List.of(new CreateColumnRequest("Col2", "TEXT", null)));
        
        ValidationException ex = assertThrows(ValidationException.class, () -> tableService.createTable(request2));
        assertTrue(ex.getFieldErrors().stream().anyMatch(e -> e.code().equals("DUPLICATE_NAME")));
    }
}
