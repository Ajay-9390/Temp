package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Compact evidence view for list/table rendering.
 */
public record EvidenceSummaryResponse(
        UUID id,
        String title,
        EvidenceCategory category,
        UUID programId,
        UUID academicYearId,
        UUID criterionId,
        UUID requirementId,
        EvidenceStatus status,
        int currentVersion,
        String latestFileType,
        String uploadedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
