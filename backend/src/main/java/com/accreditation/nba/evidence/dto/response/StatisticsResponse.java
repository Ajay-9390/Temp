package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import java.util.Map;
import java.util.UUID;

/**
 * Aggregated evidence statistics for the dashboard. {@code required} and {@code missing}
 * are derived from the external requirement provider (gap detection).
 */
public record StatisticsResponse(
        UUID programId,
        UUID academicYearId,
        long totalEvidence,
        long required,
        long draft,
        long submitted,
        long underReview,
        long changesRequired,
        long approved,
        long rejected,
        long archived,
        long missing,
        Map<EvidenceStatus, Long> byStatus,
        Map<EvidenceCategory, Long> byCategory
) {
}
