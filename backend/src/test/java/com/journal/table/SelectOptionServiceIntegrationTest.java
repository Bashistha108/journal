package com.journal.table;

import com.journal.common.ConflictException;
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
class SelectOptionServiceIntegrationTest {

    @Autowired
    private TableService tableService;

    @Autowired
    private SelectOptionService selectOptionService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldAddAndRejectDeletionIfInUse() {
        TableDetail table = tableService.createTable(new CreateTableRequest(
                "Table Options",
                List.of(new CreateColumnRequest("Status", "SELECT", List.of("Pending", "Done")))
        ));
        
        Long colId = table.columns().get(0).id();
        Long doneOptId = table.columns().get(0).selectOptions().get(1).id();

        selectOptionService.addOptions(table.id(), colId, new AddSelectOptionsRequest(List.of("Archived")));
        
        // Simulate row save manually using physical identifiers
        jdbcTemplate.update("INSERT INTO tj_data.t_" + table.id() + " (c_" + colId + ") VALUES (?)", doneOptId);

        ConflictException ex = assertThrows(ConflictException.class, () -> 
            selectOptionService.deleteOption(table.id(), colId, doneOptId)
        );
        assertEquals("SELECT_VALUE_IN_USE", ex.getCode());

        // Deleting unused option should work
        Long pendingOptId = table.columns().get(0).selectOptions().get(0).id();
        assertDoesNotThrow(() -> selectOptionService.deleteOption(table.id(), colId, pendingOptId));
    }

    @Test
    void shouldPreventDeletingLastOption() {
        TableDetail table = tableService.createTable(new CreateTableRequest(
                "Table One Opt",
                List.of(new CreateColumnRequest("Type", "SELECT", List.of("Only")))
        ));

        Long colId = table.columns().get(0).id();
        Long optId = table.columns().get(0).selectOptions().get(0).id();

        ValidationException ex = assertThrows(ValidationException.class, () -> 
            selectOptionService.deleteOption(table.id(), colId, optId)
        );
        assertTrue(ex.getFieldErrors().stream().anyMatch(e -> e.code().equals("NOT_ALLOWED")));
    }
}
