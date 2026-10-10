package com.journal.table;

import java.text.Normalizer;

public class DisplayNameNormalizer {
    public static String normalize(String displayName) {
        if (displayName == null) {
            return "";
        }
        String nfdNormalizedString = Normalizer.normalize(displayName, Normalizer.Form.NFD); 
        String withoutDiacritics = nfdNormalizedString.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutDiacritics.trim().toLowerCase().replaceAll("[^a-z0-9\\s+\\-]", "").replaceAll("\\s+", " ");
    }
}
