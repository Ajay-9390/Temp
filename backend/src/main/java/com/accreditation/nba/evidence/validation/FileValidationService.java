package com.accreditation.nba.evidence.validation;

import com.accreditation.nba.evidence.config.EvidenceProperties;
import com.accreditation.nba.evidence.exception.FileValidationException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

/**
 * Validates uploaded files before they are stored. Security-oriented: never trusts the
 * client-supplied extension or content type alone.
 *
 * <ul>
 *   <li>rejects empty files and files over the configured size limit;</li>
 *   <li>sanitises the file name to a safe character set (prevents path traversal);</li>
 *   <li>detects the true MIME type from content via Apache Tika;</li>
 *   <li>cross-checks the detected type against the declared extension;</li>
 *   <li>computes a SHA-256 checksum (used for versioning / duplicate detection).</li>
 * </ul>
 * Uploaded files are never executed.
 */
@Slf4j
@Service
public class FileValidationService {

    private final EvidenceProperties properties;
    private final Tika tika = new Tika();

    /** Tolerant map of extension &rarr; acceptable detected MIME types. */
    private static final Map<String, Set<String>> EXTENSION_MIME_TYPES = Map.ofEntries(
            Map.entry("pdf", Set.of("application/pdf")),
            Map.entry("doc", Set.of("application/msword", "application/x-tika-msoffice",
                    "application/x-tika-ole2")),
            Map.entry("docx", Set.of(
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/x-tika-ooxml", "application/zip")),
            Map.entry("xls", Set.of("application/vnd.ms-excel", "application/x-tika-msoffice",
                    "application/x-tika-ole2")),
            Map.entry("xlsx", Set.of(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/x-tika-ooxml", "application/zip")),
            Map.entry("ppt", Set.of("application/vnd.ms-powerpoint", "application/x-tika-msoffice",
                    "application/x-tika-ole2")),
            Map.entry("pptx", Set.of(
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/x-tika-ooxml", "application/zip")),
            Map.entry("jpg", Set.of("image/jpeg")),
            Map.entry("jpeg", Set.of("image/jpeg")),
            Map.entry("png", Set.of("image/png")),
            Map.entry("csv", Set.of("text/csv", "text/plain", "application/csv")));

    public FileValidationService(EvidenceProperties properties) {
        this.properties = properties;
    }

    public ValidatedFile validate(byte[] content, String originalFilename) {
        if (content == null || content.length == 0) {
            throw FileValidationException.empty();
        }

        long max = properties.getValidation().getMaxFileSizeBytes();
        if (content.length > max) {
            throw FileValidationException.tooLarge(content.length, max);
        }

        String safeName = sanitizeFileName(originalFilename);
        String extension = extractExtension(safeName);

        List<String> allowed = properties.getValidation().getAllowedExtensions();
        if (!allowed.contains(extension)) {
            throw FileValidationException.unsupportedExtension(extension.isBlank() ? "(none)" : extension);
        }

        String detectedMime = detectMime(content);
        if (!isContentConsistent(extension, detectedMime)) {
            throw FileValidationException.contentMismatch(extension, detectedMime);
        }

        String checksum = sha256(content);
        return new ValidatedFile(safeName, extension, detectedMime, content.length, checksum);
    }

    /** Reduce an arbitrary client file name to a safe, path-traversal-proof name. */
    public String sanitizeFileName(String original) {
        if (original == null || original.isBlank()) {
            throw FileValidationException.unsafeName();
        }
        // Strip any directory component (handles both / and \ separators).
        String name = original.replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        name = name.trim();
        if (name.isEmpty() || name.equals(".") || name.equals("..") || name.contains("..")) {
            throw FileValidationException.unsafeName();
        }
        // Whitelist: letters, digits, dot, underscore, hyphen. Everything else becomes '_'.
        String sanitized = name.replaceAll("[^A-Za-z0-9._-]", "_");
        // Avoid leading dots (hidden files) and cap length.
        sanitized = sanitized.replaceAll("^\\.+", "");
        if (sanitized.length() > 200) {
            String ext = extractExtension(sanitized);
            String base = sanitized.substring(0, Math.min(sanitized.length(), 180));
            sanitized = ext.isBlank() ? base : base + "." + ext;
        }
        if (sanitized.isBlank() || sanitized.startsWith(".") || sanitized.endsWith(".")) {
            throw FileValidationException.unsafeName();
        }
        return sanitized;
    }

    private String extractExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot <= 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String detectMime(byte[] content) {
        try {
            return tika.detect(content);
        } catch (Exception e) {
            log.warn("MIME detection failed: {}", e.getMessage());
            return "application/octet-stream";
        }
    }

    private boolean isContentConsistent(String extension, String detectedMime) {
        Set<String> expected = EXTENSION_MIME_TYPES.get(extension);
        if (expected == null) {
            return false;
        }
        return expected.contains(detectedMime.toLowerCase(Locale.ROOT));
    }

    private String sha256(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(content));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
