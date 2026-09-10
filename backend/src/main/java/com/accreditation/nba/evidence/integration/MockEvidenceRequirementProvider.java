package com.accreditation.nba.evidence.integration;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * Development-phase stand-in for the NBA Criteria module. Returns a representative, stable
 * set of required evidence so gap detection, statistics and reports work end-to-end before
 * the real module exists. Criterion/requirement UUIDs are derived deterministically from
 * their codes so they remain consistent across calls.
 *
 * <p>Registered as the default {@link EvidenceRequirementProvider} bean in
 * {@code DefaultIntegrationConfig} via {@code @ConditionalOnMissingBean}, so a real client
 * (e.g. {@code NbaCriteriaRequirementClient}) automatically replaces it when added.
 */
public class MockEvidenceRequirementProvider implements EvidenceRequirementProvider {

    @Override
    public List<RequiredEvidence> getRequiredEvidence(UUID programId, UUID academicYearId) {
        return List.of(
                required("1", "1.1", EvidenceCategory.ADMINISTRATIVE, "Institutional vision & mission documents", true),
                required("1", "1.2", EvidenceCategory.ACADEMIC, "PEO definition & dissemination records", true),
                required("2", "2.1", EvidenceCategory.COURSE_FILE, "Curriculum & syllabus documents", true),
                required("2", "2.2", EvidenceCategory.COURSE_FILE, "Course files with CO definitions", true),
                required("3", "3.1", EvidenceCategory.ASSESSMENT, "Internal assessment records", true),
                required("3", "3.2", EvidenceCategory.ASSESSMENT, "CO attainment computation sheets", true),
                required("4", "4.1", EvidenceCategory.STUDENT, "Student admission & performance records", true),
                required("5", "5.1", EvidenceCategory.FACULTY, "Faculty qualification & appointment records", true),
                required("5", "5.2", EvidenceCategory.TRAINING, "Faculty development program certificates", true),
                required("5", "5.3", EvidenceCategory.RESEARCH, "Research publications & funded projects", false),
                required("6", "6.1", EvidenceCategory.INFRASTRUCTURE, "Classroom & seminar hall records", true),
                required("6", "6.2", EvidenceCategory.LABORATORY, "Laboratory & equipment records", true),
                required("7", "7.1", EvidenceCategory.PLACEMENT, "Placement & higher-studies records", false),
                required("8", "8.1", EvidenceCategory.FEEDBACK, "Stakeholder feedback & analysis", false));
    }

    private RequiredEvidence required(String criterionCode, String requirementCode,
                                      EvidenceCategory category, String name, boolean mandatory) {
        return new RequiredEvidence(
                deterministicId("criterion-" + criterionCode),
                deterministicId("requirement-" + requirementCode),
                "C" + criterionCode,
                "R" + requirementCode,
                category,
                name,
                mandatory);
    }

    private UUID deterministicId(String seed) {
        return UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
    }
}
