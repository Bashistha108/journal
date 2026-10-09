package com.journal.schema;

public class SqlIdentifiers {
    public static String quote(String identifier) {
        if (identifier == null) {
            throw new IllegalArgumentException("Identifier cannot be null");
        }
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}
