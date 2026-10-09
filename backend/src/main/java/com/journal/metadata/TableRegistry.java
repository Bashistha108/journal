package com.journal.metadata;

import java.time.OffsetDateTime;

public record TableRegistry(
    Long tableId,
    String displayName,
    String normalizedName,
    String physicalName,
    OffsetDateTime createdAt
) {}
