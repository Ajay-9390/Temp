package com.accreditation.nba.evidence.validation;

/**
 * Outcome of successful file validation: the sanitised file name, normalised extension, the
 * MIME type detected from content (not the client-supplied one), size, and SHA-256 checksum.
 */
public record ValidatedFile(
        String fileName,
        String extension,
        String mimeType,
        long size,
        String checksum
) {
}
