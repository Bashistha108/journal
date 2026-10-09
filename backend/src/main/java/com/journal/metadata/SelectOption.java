package com.journal.metadata;

public record SelectOption(
    Long optionId,
    Long columnId,
    String value,
    String normalizedValue,
    Integer position
) {}
