package com.journal.metadata;

import com.journal.support.PostgresTestContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(initializers = PostgresTestContainer.class)
@Import(PostgresTestContainer.DatabaseGuardConfiguration.class)
@Transactional
class TableRegistryRepositoryTest {

    @Autowired
    private TableRegistryRepository tableRegistryRepository;

    @Test
    void shouldSaveAndRetrieveTableRegistry() {
        TableRegistry table = new TableRegistry(null, "Test Table", "test_table", "t_123", null);
        TableRegistry saved = tableRegistryRepository.save(table);

        assertNotNull(saved.tableId());
        assertEquals("Test Table", saved.displayName());
        assertNotNull(saved.createdAt());

        Optional<TableRegistry> retrieved = tableRegistryRepository.findById(saved.tableId());
        assertTrue(retrieved.isPresent());
        assertEquals(saved.tableId(), retrieved.get().tableId());
    }
}
