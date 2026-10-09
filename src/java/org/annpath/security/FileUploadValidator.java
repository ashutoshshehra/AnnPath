package org.annpath.security;

import org.annpath.config.AppConfig;

import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Validates uploaded files (NGO documents, food images, proof photos)
 * checking file signatures, MIME types, extensions, size limits, and safe storage paths.
 */
public final class FileUploadValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(
            Arrays.asList("pdf", "jpg", "jpeg", "png")
    );

    private static final Set<String> ALLOWED_MIME_TYPES = new HashSet<>(
            Arrays.asList("application/pdf", "image/jpeg", "image/png", "image/jpg")
    );

    private FileUploadValidator() {}

    /**
     * Validate an uploaded Servlet 3.0 Part.
     */
    public static void validateFile(Part part) throws IllegalArgumentException, IOException {
        if (part == null || part.getSize() == 0) {
            throw new IllegalArgumentException("No file provided.");
        }

        if (part.getSize() > AppConfig.getMaxFileSizeBytes()) {
            throw new IllegalArgumentException("File size exceeds limit of " + (AppConfig.getMaxFileSizeBytes() / (1024 * 1024)) + " MB.");
        }

        String submittedName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
        String ext = getFileExtension(submittedName).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("Invalid file extension: " + ext + ". Allowed: PDF, JPG, PNG.");
        }

        String contentType = part.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type: " + contentType);
        }

        // Validate magic bytes to prevent executable spoofing
        try (InputStream in = part.getInputStream()) {
            byte[] header = new byte[8];
            int read = in.read(header);
            if (read < 4) {
                throw new IllegalArgumentException("Corrupted or empty file stream.");
            }
            if (!isValidMagicBytes(header, ext)) {
                throw new IllegalArgumentException("File content signature does not match declared format.");
            }
        }
    }

    private static boolean isValidMagicBytes(byte[] header, String ext) {
        if ("pdf".equals(ext)) {
            // %PDF
            return header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46;
        } else if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            // FF D8 FF
            return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8 && (header[2] & 0xFF) == 0xFF;
        } else if ("png".equals(ext)) {
            // 89 50 4E 47
            return (header[0] & 0xFF) == 0x89 && (header[1] & 0xFF) == 0x50 && (header[2] & 0xFF) == 0x4E && (header[3] & 0xFF) == 0x47;
        }
        return false;
    }

    public static String getFileExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot >= 0 && dot < fileName.length() - 1) {
            return fileName.substring(dot + 1);
        }
        return "";
    }

    /**
     * Generate collision-free sanitized stored filename.
     */
    public static String generateStoredFileName(String originalFileName) {
        String ext = getFileExtension(originalFileName).toLowerCase();
        return UUID.randomUUID().toString() + (ext.isEmpty() ? "" : "." + ext);
    }

    /**
     * Ensures target upload directory exists.
     */
    public static File getOrCreateUploadDir() {
        File dir = new File(AppConfig.getUploadDirectory());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }
}
