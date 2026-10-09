package com.journal.schema;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PhysicalNamingTest {
    @Test
    void shouldGenerateCorrectTableNames() {
        assertEquals("t_123", PhysicalNaming.table(123L));
    }

    @Test
    void shouldGenerateCorrectTableAttachmentNames() {
        assertEquals("t_123_att", PhysicalNaming.tableAttachment(123L));
    }

    @Test
    void shouldGenerateCorrectColumnNames() {
        assertEquals("c_456", PhysicalNaming.column(456L));
    }
}
