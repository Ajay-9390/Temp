package com.accreditation.nba.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.accreditation.nba.evidence.dto.request.CreateMappingRequest;
import com.accreditation.nba.evidence.dto.response.MappingResponse;
import com.accreditation.nba.evidence.entity.NbaEvidenceMapping;
import com.accreditation.nba.evidence.enums.MappingType;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.event.DomainEventPublisher;
import com.accreditation.nba.evidence.exception.DuplicateMappingException;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.repository.NbaEvidenceMappingRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MappingServiceTest {

    @Mock
    private NbaEvidenceRepository evidenceRepository;
    @Mock
    private NbaEvidenceMappingRepository mappingRepository;
    @Mock
    private EvidenceAuditService auditService;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private CurrentUserProvider currentUser;

    private MappingService mappingService;

    private final UUID evidenceId = UUID.randomUUID();
    private final UUID criterionId = UUID.randomUUID();
    private final UUID requirementId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mappingService = new MappingService(evidenceRepository, mappingRepository,
                new EvidenceMapper(), auditService, eventPublisher, currentUser);
    }

    @Test
    void addsMappingDefaultingToSupporting() {
        when(evidenceRepository.existsById(evidenceId)).thenReturn(true);
        when(mappingRepository.existsByEvidenceIdAndCriterionIdAndRequirementId(evidenceId, criterionId, requirementId))
                .thenReturn(false);
        when(currentUser.currentUserId()).thenReturn("tester");
        when(mappingRepository.save(any(NbaEvidenceMapping.class))).thenAnswer(inv -> inv.getArgument(0));

        MappingResponse response = mappingService.addMapping(evidenceId,
                new CreateMappingRequest(criterionId, requirementId, null));

        assertThat(response.mappingType()).isEqualTo(MappingType.SUPPORTING);
        assertThat(response.criterionId()).isEqualTo(criterionId);
        assertThat(response.createdBy()).isEqualTo("tester");
    }

    @Test
    void rejectsDuplicateMapping() {
        when(evidenceRepository.existsById(evidenceId)).thenReturn(true);
        when(mappingRepository.existsByEvidenceIdAndCriterionIdAndRequirementId(evidenceId, criterionId, requirementId))
                .thenReturn(true);

        assertThatThrownBy(() -> mappingService.addMapping(evidenceId,
                new CreateMappingRequest(criterionId, requirementId, MappingType.PRIMARY)))
                .isInstanceOf(DuplicateMappingException.class);
    }
}
