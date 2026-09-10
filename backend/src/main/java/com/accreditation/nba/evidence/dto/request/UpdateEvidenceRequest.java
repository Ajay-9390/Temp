package com.accreditation.nba.evidence.dto.request;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Payload to update evidence metadata. Not permitted on ARCHIVED evidence.
 */
public record UpdateEvidenceRequest(

        @NotBlank
        @Size(max = 255)
        String title,

        @Size(max = 5000)
        String description,

        @NotNull
        EvidenceCategory category,

        UUID programId,
        UUID departmentId,
        UUID academicYearId,
        UUID criterionId,
        UUID requirementId,

        List<String> tags
) {
}
