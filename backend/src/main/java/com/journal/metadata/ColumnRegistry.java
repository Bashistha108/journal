package com.journal.metadata;

import java.time.OffsetDateTime;

public record ColumnRegistry(
    Long columnId,
    Long tableId,
    Integer position,
    String displayName,
    String normalizedName,
    String dataType,
    String physicalName,
    OffsetDateTime createdAt
) {}
