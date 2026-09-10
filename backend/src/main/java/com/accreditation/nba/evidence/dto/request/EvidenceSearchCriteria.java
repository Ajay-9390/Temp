package com.accreditation.nba.evidence.dto.request;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Structured search/filter criteria for listing evidence. All fields are optional and
 * combined with AND semantics. {@code keyword} matches title, description, tags and
 * (via a version sub-query) file name and extracted text.
 */
public record EvidenceSearchCriteria(
        UUID programId,
        UUID departmentId,
        UUID academicYearId,
        UUID criterionId,
        UUID requirementId,
        EvidenceCategory category,
        EvidenceStatus status,
        String fileType,
        String uploadedBy,
        String keyword,
        OffsetDateTime createdFrom,
        OffsetDateTime createdTo
) {
}
