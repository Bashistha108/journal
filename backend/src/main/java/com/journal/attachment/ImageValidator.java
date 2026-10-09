package com.journal.attachment;

import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ImageValidator {
    public static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MiB
    public static final int MAX_DIMENSION = 20000;
    public static final long MAX_PIXELS = 100_000_000;

    public record ImageInfo(String contentType, String safeFilename, int width, int height) {}

    public static ImageInfo validate(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("File size exceeds 10 MiB limit");
        }

        String safeFilename = sanitizeFilename(file.getOriginalFilename());

        try (InputStream is = new BufferedInputStream(file.getInputStream())) {
            is.mark(1024 * 64);
            byte[] header = new byte[30];
            int read = readFully(is, header);
            if (read < 8) {
                throw new IllegalArgumentException("File is too small");
            }

            if (isPng(header)) {
                return parsePng(header, safeFilename);
            } else if (isWebp(header)) {
                return parseWebp(header, safeFilename);
            } else if (isJpeg(header)) {
                is.reset();
                return parseJpeg(is, safeFilename);
            } else {
                throw new IllegalArgumentException("Unsupported image format");
            }
        }
    }

    private static String sanitizeFilename(String filename) {
        if (filename == null || filename.isBlank()) return "attachment";
        String name = filename.replaceAll(".*[/\\\\]", "");
        name = name.replaceAll("\\p{Cntrl}", "");
        if (name.length() > 255) {
            name = name.substring(0, 255);
        }
        if (name.isEmpty()) return "attachment";
        return name;
    }

    private static void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Invalid image dimensions");
        }
        if (width > MAX_DIMENSION || height > MAX_DIMENSION) {
            throw new IllegalArgumentException("Image dimensions exceed limit");
        }
        long pixels = (long) width * height;
        if (pixels > MAX_PIXELS) {
            throw new IllegalArgumentException("Image pixels exceed limit");
        }
    }

    private static int readFully(InputStream is, byte[] b) throws IOException {
        int total = 0;
        while (total < b.length) {
            int result = is.read(b, total, b.length - total);
            if (result == -1) {
                break;
            }
            total += result;
        }
        return total;
    }

    private static boolean isPng(byte[] header) {
        return header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47
                && header[4] == 0x0D && header[5] == 0x0A && header[6] == 0x1A && header[7] == 0x0A;
    }

    private static ImageInfo parsePng(byte[] header, String filename) {
        if (header.length < 24) throw new IllegalArgumentException("Truncated PNG");
        if (header[12] != 'I' || header[13] != 'H' || header[14] != 'D' || header[15] != 'R') {
            throw new IllegalArgumentException("Invalid PNG: missing IHDR");
        }
        ByteBuffer bb = ByteBuffer.wrap(header, 16, 8);
        int width = bb.getInt();
        int height = bb.getInt();
        validateDimensions(width, height);
        return new ImageInfo("image/png", filename, width, height);
    }

    private static boolean isWebp(byte[] header) {
        return header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header.length >= 12
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
    }

    private static ImageInfo parseWebp(byte[] header, String filename) {
        if (header.length < 30) throw new IllegalArgumentException("Truncated WebP");
        String chunkType = new String(header, 12, 4, StandardCharsets.US_ASCII);
        int width = 0, height = 0;

        if (chunkType.equals("VP8X")) {
            width = 1 + ((header[24] & 0xFF) | ((header[25] & 0xFF) << 8) | ((header[26] & 0xFF) << 16));
            height = 1 + ((header[27] & 0xFF) | ((header[28] & 0xFF) << 8) | ((header[29] & 0xFF) << 16));
        } else if (chunkType.equals("VP8 ")) {
            if ((header[23] & 0xFF) != 0x9D || (header[24] & 0xFF) != 0x01 || (header[25] & 0xFF) != 0x2A) {
                throw new IllegalArgumentException("Invalid VP8 start code");
            }
            width = ((header[26] & 0xFF) | ((header[27] & 0xFF) << 8)) & 0x3FFF;
            height = ((header[28] & 0xFF) | ((header[29] & 0xFF) << 8)) & 0x3FFF;
        } else if (chunkType.equals("VP8L")) {
            if (header[20] != 0x2F) throw new IllegalArgumentException("Invalid VP8L signature");
            int b1 = header[21] & 0xFF;
            int b2 = header[22] & 0xFF;
            int b3 = header[23] & 0xFF;
            int b4 = header[24] & 0xFF;
            width = 1 + (b1 | ((b2 & 0x3F) << 8));
            height = 1 + (((b2 & 0xC0) >> 6) | (b3 << 2) | ((b4 & 0x0F) << 10));
        } else {
            throw new IllegalArgumentException("Unsupported WebP format: " + chunkType);
        }

        validateDimensions(width, height);
        return new ImageInfo("image/webp", filename, width, height);
    }

    private static boolean isJpeg(byte[] header) {
        return header[0] == (byte) 0xFF && header[1] == (byte) 0xD8;
    }

    private static ImageInfo parseJpeg(InputStream is, String filename) throws IOException {
        int b1 = is.read();
        int b2 = is.read();
        if (b1 != 0xFF || b2 != 0xD8) {
            throw new IllegalArgumentException("Invalid JPEG signature");
        }

        while (true) {
            int marker = -1;
            while (true) {
                int b = is.read();
                if (b == -1) throw new IllegalArgumentException("Truncated JPEG");
                if (b == 0xFF) {
                    int next = is.read();
                    if (next != 0xFF && next != 0x00) {
                        marker = next;
                        break;
                    }
                }
            }

            if (marker == 0xD9 || marker == 0xDA) { // EOI or SOS
                throw new IllegalArgumentException("JPEG missing SOF marker");
            }

            int lenHigh = is.read();
            int lenLow = is.read();
            if (lenHigh == -1 || lenLow == -1) throw new IllegalArgumentException("Truncated JPEG");
            int len = (lenHigh << 8) | lenLow;

            // SOF0, SOF1, SOF2 (baseline, extended sequential, progressive)
            if (marker == 0xC0 || marker == 0xC1 || marker == 0xC2) {
                if (len < 5) throw new IllegalArgumentException("Invalid SOF length");
                is.read(); // precision
                int hHigh = is.read();
                int hLow = is.read();
                int wHigh = is.read();
                int wLow = is.read();
                if (hHigh == -1 || wLow == -1) throw new IllegalArgumentException("Truncated JPEG");
                
                int height = (hHigh << 8) | hLow;
                int width = (wHigh << 8) | wLow;
                
                validateDimensions(width, height);
                return new ImageInfo("image/jpeg", filename, width, height);
            } else {
                long skipped = is.skip(len - 2);
                if (skipped != len - 2) {
                    throw new IllegalArgumentException("Truncated JPEG");
                }
            }
        }
    }
}
