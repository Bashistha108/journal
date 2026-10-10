package com.journal.table;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DisplayNameNormalizerTest {
    @Test
    void shouldNormalizeNames() {
        assertEquals("profitloss", DisplayNameNormalizer.normalize("Profit/Loss"));
        assertEquals("trade date", DisplayNameNormalizer.normalize("Trade Date"));
        assertEquals("cafe", DisplayNameNormalizer.normalize("Café"));
        assertEquals("hello world", DisplayNameNormalizer.normalize("  hello   world  "));
        assertEquals("", DisplayNameNormalizer.normalize("!!!"));
        assertEquals("a+", DisplayNameNormalizer.normalize("A+"));
        assertEquals("a-", DisplayNameNormalizer.normalize("A-"));
    }
}
