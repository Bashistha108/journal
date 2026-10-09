package com.journal.migration;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChangelogSafetyTest {

    @Test
    void changelogShouldNotContainDangerousOperations() throws Exception {
        Path changelogPath = Paths.get("src/main/resources/db/changelog/changes/001-create-internal-metadata.yaml");
        assertTrue(Files.exists(changelogPath), "Changelog file must exist");

        String content = Files.readString(changelogPath).toLowerCase();

        assertFalse(content.contains("droptable"), "Changelog must not contain dropTable");
        assertFalse(content.contains("dropcolumn"), "Changelog must not contain dropColumn");
        assertFalse(content.contains("renametable"), "Changelog must not contain renameTable");
        assertFalse(content.contains("renamecolumn"), "Changelog must not contain renameColumn");
    }
}
