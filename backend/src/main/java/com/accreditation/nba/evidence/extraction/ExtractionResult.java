package com.accreditation.nba.evidence.extraction;

/**
 * Result of synchronous, best-effort content extraction performed at upload time.
 * {@code needsOcr} signals that no text layer was found (scanned PDF / image) and OCR should
 * run asynchronously.
 */
public record ExtractionResult(
        Integer pageCount,
        String detectedLanguage,
        String text,
        boolean needsOcr
) {

    public static ExtractionResult none() {
        return new ExtractionResult(null, null, null, false);
    }

    public boolean hasText() {
        return text != null && !text.isBlank();
    }
}
