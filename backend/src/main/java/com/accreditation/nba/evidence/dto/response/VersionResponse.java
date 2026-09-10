package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.OcrStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * A single file version. {@code textPreview} is a short excerpt of extracted/OCR text
 * (the full text is intentionally not returned in list payloads).
 */
public record VersionResponse(
        UUID id,
        UUID evidenceId,
        int versionNumber,
        String fileName,
        String fileType,
        String mimeType,
        long fileSize,
        String checksum,
        String uploadedBy,
        String changeReason,
        Integer pageCount,
        String detectedLanguage,
        OcrStatus ocrStatus,
        OffsetDateTime ocrProcessedAt,
        boolean hasExtractedText,
        String textPreview,
        OffsetDateTime createdAt
) {
}
