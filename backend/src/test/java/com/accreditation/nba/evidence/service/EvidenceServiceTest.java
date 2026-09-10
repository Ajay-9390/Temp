package com.accreditation.nba.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.accreditation.nba.evidence.dto.request.CreateEvidenceRequest;
import com.accreditation.nba.evidence.dto.response.EvidenceResponse;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.event.DomainEventPublisher;
import com.accreditation.nba.evidence.exception.BadRequestException;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.repository.NbaEvidenceMappingRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceReviewRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceVersionRepository;
import com.accreditation.nba.evidence.storage.EvidenceStorageService;
import com.accreditation.nba.evidence.workflow.EvidenceWorkflow;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock private NbaEvidenceRepository evidenceRepository;
    @Mock private NbaEvidenceVersionRepository versionRepository;
    @Mock private NbaEvidenceMappingRepository mappingRepository;
    @Mock private NbaEvidenceReviewRepository reviewRepository;
    @Mock private EvidenceAuditService auditService;
    @Mock private DomainEventPublisher eventPublisher;
    @Mock private CurrentUserProvider currentUser;
    @Mock private VersionService versionService;
    @Mock private SearchService searchService;
    @Mock private EvidenceStorageService storageService;

    private EvidenceService evidenceService;

    @BeforeEach
    void setUp() {
        evidenceService = new EvidenceService(evidenceRepository, versionRepository, mappingRepository,
                reviewRepository, new EvidenceMapper(), new EvidenceWorkflow(), auditService,
                eventPublisher, currentUser, versionService, searchService, storageService);
    }

    @Test
    void createStartsInDraftWithNoVersions() {
        NbaEvidence[] holder = new NbaEvidence[1];
        when(evidenceRepository.save(any(NbaEvidence.class))).thenAnswer(inv -> {
            holder[0] = inv.getArgument(0);
            return holder[0];
        });
        when(evidenceRepository.findById(any())).thenAnswer(inv -> Optional.ofNullable(holder[0]));
        when(versionRepository.findTopByEvidenceIdOrderByVersionNumberDesc(any())).thenReturn(Optional.empty());
        when(currentUser.currentUserId()).thenReturn("mock-user");

        CreateEvidenceRequest request = new CreateEvidenceRequest(
                "FDP Certificate", "desc", EvidenceCategory.FACULTY,
                null, null, null, null, null, null);

        EvidenceResponse response = evidenceService.create(request, null);

        assertThat(response.status()).isEqualTo(EvidenceStatus.DRAFT);
        assertThat(response.currentVersion()).isZero();
        assertThat(response.title()).isEqualTo("FDP Certificate");
        assertThat(response.uploadedBy()).isEqualTo("mock-user");
        assertThat(response.latestVersion()).isNull();
    }

    @Test
    void submitWithoutFileIsRejected() {
        NbaEvidence draft = NbaEvidence.builder()
                .id(UUID.randomUUID()).title("Doc").category(EvidenceCategory.OTHER)
                .status(EvidenceStatus.DRAFT).currentVersion(0).uploadedBy("mock-user").build();
        when(evidenceRepository.findById(draft.getId())).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> evidenceService.submit(draft.getId(), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("without an uploaded file");
    }

    @Test
    void submitTransitionsDraftToSubmitted() {
        UUID id = UUID.randomUUID();
        NbaEvidence draft = NbaEvidence.builder()
                .id(id).title("Doc").category(EvidenceCategory.OTHER)
                .status(EvidenceStatus.DRAFT).currentVersion(1).uploadedBy("mock-user").build();
        when(evidenceRepository.findById(id)).thenReturn(Optional.of(draft));
        when(evidenceRepository.save(any(NbaEvidence.class))).thenAnswer(inv -> inv.getArgument(0));
        when(versionRepository.findTopByEvidenceIdOrderByVersionNumberDesc(any())).thenReturn(Optional.empty());
        lenient().when(currentUser.currentUserId()).thenReturn("mock-user");

        EvidenceResponse response = evidenceService.submit(id, "Ready for review");

        assertThat(response.status()).isEqualTo(EvidenceStatus.SUBMITTED);
    }
}
