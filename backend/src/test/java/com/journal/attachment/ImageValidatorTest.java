package com.journal.attachment;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ImageValidatorTest {

    private MultipartFile loadFixture(String filename) throws IOException {
        ClassPathResource resource = new ClassPathResource("images/" + filename);
        return new MockMultipartFile("file", filename, "image/jpeg", resource.getInputStream());
    }

    @Test
    void testValidPng() throws IOException {
        ImageValidator.ImageInfo info = ImageValidator.validate(loadFixture("valid.png"));
        assertEquals("image/png", info.contentType());
        assertEquals(10, info.width());
        assertEquals(10, info.height());
        assertEquals("valid.png", info.safeFilename());
    }

    @Test
    void testValidJpeg() throws IOException {
        ImageValidator.ImageInfo info = ImageValidator.validate(loadFixture("valid.jpg"));
        assertEquals("image/jpeg", info.contentType());
        assertEquals(10, info.width());
        assertEquals(10, info.height());
        assertEquals("valid.jpg", info.safeFilename());
    }

    @Test
    void testValidWebp() throws IOException {
        ImageValidator.ImageInfo info = ImageValidator.validate(loadFixture("valid.webp"));
        assertEquals("image/webp", info.contentType());
        assertEquals(10, info.width());
        assertEquals(10, info.height());
        assertEquals("valid.webp", info.safeFilename());
    }

    @Test
    void testTruncatedFile() throws IOException {
        MultipartFile file = loadFixture("truncated.png");
        assertThrows(IllegalArgumentException.class, () -> ImageValidator.validate(file));
    }

    @Test
    void testMislabeledFile() throws IOException {
        // A PNG saved as .jpg. Validator should ignore the extension and client content type, and return image/png
        ImageValidator.ImageInfo info = ImageValidator.validate(loadFixture("mislabeled.jpg"));
        assertEquals("image/png", info.contentType());
        assertEquals(10, info.width());
        assertEquals(10, info.height());
        assertEquals("mislabeled.jpg", info.safeFilename());
    }

    @Test
    void testPolyglotFile() throws IOException {
        // Unsupported or malformed due to wrong bytes at start
        MultipartFile file = loadFixture("polyglot.png");
        assertThrows(IllegalArgumentException.class, () -> ImageValidator.validate(file));
    }

    @Test
    void testExact10MiB() throws IOException {
        ImageValidator.ImageInfo info = ImageValidator.validate(loadFixture("exact_10mib.png"));
        assertEquals("image/png", info.contentType());
    }

    @Test
    void testOversize10MiB() throws IOException {
        // Need a custom mock because loading a 10MiB byte array into memory works but MockMultipartFile checks size.
        // Wait, the file on disk is actually 10MB + 1. We just load it.
        MultipartFile file = loadFixture("oversize_10mib.png");
        assertThrows(IllegalArgumentException.class, () -> ImageValidator.validate(file));
    }

    @Test
    void testOversizeDimensions() throws IOException {
        MultipartFile file = loadFixture("oversize_dim.png");
        assertThrows(IllegalArgumentException.class, () -> ImageValidator.validate(file));
    }

    @Test
    void testEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);
        assertThrows(IllegalArgumentException.class, () -> ImageValidator.validate(file));
    }
    
    @Test
    void testNullFile() {
        assertThrows(IllegalArgumentException.class, () -> ImageValidator.validate(null));
    }
}
