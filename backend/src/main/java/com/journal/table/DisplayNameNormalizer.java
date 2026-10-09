package com.journal.table;

import java.text.Normalizer;

public class DisplayNameNormalizer {
    public static String normalize(String displayName) {
        if (displayName == null) {
            return "";
        }
        String normalized = Normalizer.normalize(displayName, Normalizer.Form.NFKC);
        // Lowercase, replace multiple spaces with single space, remove non-alphanumeric
        return normalized.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "").replaceAll("\\s+", " ");
    }
}
