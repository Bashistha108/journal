package com.journal.schema;

import java.util.regex.Pattern;

public class ReservedNamespacePolicy {
    private static final Pattern VALID_PHYSICAL_PATTERN = Pattern.compile("^(t_\\d+|t_\\d+_att|c_\\d+)$");

    public static void validatePhysicalName(String name) {
        if (!VALID_PHYSICAL_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid physical name: " + name);
        }
    }

    public static void checkReservedPrefix(String displayName) {
        if (displayName != null && displayName.toLowerCase().startsWith("tj_")) {
            throw new IllegalArgumentException("Name cannot start with reserved prefix 'tj_': " + displayName);
        }
    }

    public static void checkReservedSchema(String schemaName) {
        if ("tj_meta".equalsIgnoreCase(schemaName) || "tj_data".equalsIgnoreCase(schemaName)) {
            throw new IllegalArgumentException("Reserved schema name: " + schemaName);
        }
    }
}
