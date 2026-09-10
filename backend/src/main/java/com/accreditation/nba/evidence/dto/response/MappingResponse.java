package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.MappingType;
import java.time.OffsetDateTime;
import java.util.UUID;

public record MappingResponse(
        UUID id,
        UUID evidenceId,
        UUID criterionId,
        UUID requirementId,
        MappingType mappingType,
        String createdBy,
        OffsetDateTime createdAt
) {
}
