package com.journal.schema;

public class PhysicalNaming {
    public static String table(Long tableId) {
        return "t_" + tableId;
    }

    public static String tableAttachment(Long tableId) {
        return "t_" + tableId + "_att";
    }

    public static String column(Long columnId) {
        return "c_" + columnId;
    }
}
