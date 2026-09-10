package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.enums.ReviewDecision;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ReviewResponse(
        UUID id,
        UUID evidenceId,
        Integer versionNumber,
        String reviewer,
        ReviewDecision decision,
        EvidenceStatus resultingStatus,
        String comments,
        OffsetDateTime reviewedAt
) {
}
