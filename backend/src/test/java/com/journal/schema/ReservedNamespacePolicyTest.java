package com.journal.schema;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReservedNamespacePolicyTest {
    @Test
    void shouldAcceptValidPhysicalNames() {
        assertDoesNotThrow(() -> ReservedNamespacePolicy.validatePhysicalName("t_123"));
        assertDoesNotThrow(() -> ReservedNamespacePolicy.validatePhysicalName("t_123_att"));
        assertDoesNotThrow(() -> ReservedNamespacePolicy.validatePhysicalName("c_456"));
    }

    @Test
    void shouldRejectInvalidPhysicalNames() {
        assertThrows(IllegalArgumentException.class, () -> ReservedNamespacePolicy.validatePhysicalName("tj_meta"));
        assertThrows(IllegalArgumentException.class, () -> ReservedNamespacePolicy.validatePhysicalName("t_abc"));
    }

    @Test
    void shouldRejectReservedPrefixes() {
        assertThrows(IllegalArgumentException.class, () -> ReservedNamespacePolicy.checkReservedPrefix("tj_something"));
        assertThrows(IllegalArgumentException.class, () -> ReservedNamespacePolicy.checkReservedPrefix("TJ_Something"));
        assertDoesNotThrow(() -> ReservedNamespacePolicy.checkReservedPrefix("my_table"));
    }

    @Test
    void shouldRejectReservedSchemas() {
        assertThrows(IllegalArgumentException.class, () -> ReservedNamespacePolicy.checkReservedSchema("tj_meta"));
        assertThrows(IllegalArgumentException.class, () -> ReservedNamespacePolicy.checkReservedSchema("tj_data"));
        assertDoesNotThrow(() -> ReservedNamespacePolicy.checkReservedSchema("public"));
    }
}
