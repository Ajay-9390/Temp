package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Full evidence view including counts and the latest version summary.
 */
public record EvidenceResponse(
        UUID id,
        String title,
        String description,
        EvidenceCategory category,
        UUID programId,
        UUID departmentId,
        UUID academicYearId,
        UUID criterionId,
        UUID requirementId,
        EvidenceStatus status,
        int currentVersion,
        List<String> tags,
        String uploadedBy,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        long versionCount,
        long mappingCount,
        long reviewCount,
        VersionResponse latestVersion
) {
}
