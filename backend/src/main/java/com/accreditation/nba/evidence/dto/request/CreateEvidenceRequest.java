package com.accreditation.nba.evidence.dto.request;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Payload to create a new evidence item (starts in DRAFT). External IDs are optional so
 * evidence can be drafted before all references are known.
 */
@Schema(description = "Create a new evidence item")
public record CreateEvidenceRequest(

        @NotBlank
        @Size(max = 255)
        @Schema(example = "FDP Certificate - AI/ML")
        String title,

        @Size(max = 5000)
        String description,

        @NotNull
        EvidenceCategory category,

        @Schema(description = "External NBA Program Management ID")
        UUID programId,

        UUID departmentId,

        @Schema(description = "External Calendar/Academic Year ID")
        UUID academicYearId,

        @Schema(description = "External NBA Criteria Management criterion ID")
        UUID criterionId,

        @Schema(description = "External NBA Criteria Management requirement ID")
        UUID requirementId,

        List<String> tags
) {
}
