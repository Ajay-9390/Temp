package com.accreditation.nba.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.accreditation.nba.evidence.dto.response.GapReportResponse;
import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.integration.EvidenceRequirementProvider;
import com.accreditation.nba.evidence.integration.RequiredEvidence;
import com.accreditation.nba.evidence.repository.NbaEvidenceMappingRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.view.RequirementCoverageView;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GapServiceTest {

    @Mock
    private EvidenceRequirementProvider requirementProvider;
    @Mock
    private NbaEvidenceMappingRepository mappingRepository;
    @Mock
    private NbaEvidenceRepository evidenceRepository;

    @InjectMocks
    private GapService gapService;

    private static final UUID PROGRAM = UUID.randomUUID();
    private static final UUID AY = UUID.randomUUID();
    private static final UUID C1 = UUID.randomUUID();
    private static final UUID R1 = UUID.randomUUID();
    private static final UUID C2 = UUID.randomUUID();
    private static final UUID R2 = UUID.randomUUID();

    @Test
    void computesApprovedAndMissingCoverage() {
        when(requirementProvider.getRequiredEvidence(any(), any())).thenReturn(List.of(
                new RequiredEvidence(C1, R1, "C1", "R1.1", EvidenceCategory.FACULTY, "Faculty records", true),
                new RequiredEvidence(C2, R2, "C2", "R2.1", EvidenceCategory.STUDENT, "Student records", true)));

        UUID approvedEvidence = UUID.randomUUID();
        when(mappingRepository.findMappedCoverage(any(), any())).thenReturn(List.of(
                coverage(C1, R1, approvedEvidence, EvidenceStatus.APPROVED)));
        when(evidenceRepository.findPrimaryCoverage(any(), any())).thenReturn(List.of());

        GapReportResponse report = gapService.computeGap(PROGRAM, AY, null, null);

        assertThat(report.totalRequired()).isEqualTo(2);
        assertThat(report.approved()).isEqualTo(1);
        assertThat(report.missing()).isEqualTo(1);
        assertThat(report.uploaded()).isEqualTo(1);
        assertThat(report.items()).hasSize(2);
        assertThat(report.items())
                .anySatisfy(item -> {
                    assertThat(item.criterionId()).isEqualTo(C1);
                    assertThat(item.satisfied()).isTrue();
                    assertThat(item.currentStatus()).isEqualTo("APPROVED");
                })
                .anySatisfy(item -> {
                    assertThat(item.criterionId()).isEqualTo(C2);
                    assertThat(item.satisfied()).isFalse();
                    assertThat(item.currentStatus()).isEqualTo("MISSING");
                });
    }

    @Test
    void picksMostAdvancedStatusWhenMultipleCover() {
        when(requirementProvider.getRequiredEvidence(any(), any())).thenReturn(List.of(
                new RequiredEvidence(C1, R1, "C1", "R1.1", EvidenceCategory.FACULTY, "Faculty records", true)));
        when(mappingRepository.findMappedCoverage(any(), any())).thenReturn(List.of(
                coverage(C1, R1, UUID.randomUUID(), EvidenceStatus.DRAFT),
                coverage(C1, R1, UUID.randomUUID(), EvidenceStatus.APPROVED)));
        when(evidenceRepository.findPrimaryCoverage(any(), any())).thenReturn(List.of());

        GapReportResponse report = gapService.computeGap(PROGRAM, AY, null, null);

        assertThat(report.items()).hasSize(1);
        assertThat(report.items().get(0).currentStatus()).isEqualTo("APPROVED");
        assertThat(report.approved()).isEqualTo(1);
    }

    private RequirementCoverageView coverage(UUID criterionId, UUID requirementId, UUID evidenceId,
                                             EvidenceStatus status) {
        return new RequirementCoverageView() {
            @Override
            public UUID getCriterionId() {
                return criterionId;
            }

            @Override
            public UUID getRequirementId() {
                return requirementId;
            }

            @Override
            public UUID getEvidenceId() {
                return evidenceId;
            }

            @Override
            public EvidenceStatus getStatus() {
                return status;
            }
        };
    }
}
