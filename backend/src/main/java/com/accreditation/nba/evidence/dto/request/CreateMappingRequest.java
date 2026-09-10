package com.accreditation.nba.evidence.dto.request;

import com.accreditation.nba.evidence.enums.MappingType;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Map an evidence item to an NBA criterion/requirement. {@code mappingType} defaults to
 * SUPPORTING when omitted.
 */
public record CreateMappingRequest(

        @NotNull
        UUID criterionId,

        @NotNull
        UUID requirementId,

        MappingType mappingType
) {
}
