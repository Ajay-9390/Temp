package com.accreditation.nba.evidence.integration;

import java.util.List;
import java.util.UUID;

/**
 * Integration point that supplies the list of required evidence for a program + academic
 * year. Owned by the NBA Criteria &amp; SAR Management module. Replace the mock implementation
 * with a real REST/Feign client when that module is available — no other code changes needed.
 */
public interface EvidenceRequirementProvider {

    List<RequiredEvidence> getRequiredEvidence(UUID programId, UUID academicYearId);
}
