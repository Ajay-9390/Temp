package com.accreditation.nba.evidence.service;

import com.accreditation.nba.evidence.dto.request.CreateMappingRequest;
import com.accreditation.nba.evidence.dto.response.MappingResponse;
import com.accreditation.nba.evidence.entity.NbaEvidenceMapping;
import com.accreditation.nba.evidence.enums.AuditAction;
import com.accreditation.nba.evidence.enums.MappingType;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.event.DomainEventPublisher;
import com.accreditation.nba.evidence.event.EvidenceDomainEvent;
import com.accreditation.nba.evidence.event.EvidenceEventType;
import com.accreditation.nba.evidence.exception.DuplicateMappingException;
import com.accreditation.nba.evidence.exception.EvidenceNotFoundException;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.repository.NbaEvidenceMappingRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manages evidence &harr; criterion/requirement mappings. One evidence item can support many
 * requirements; the physical file is stored once and only mapping rows multiply.
 */
@Service
public class MappingService {

    private final NbaEvidenceRepository evidenceRepository;
    private final NbaEvidenceMappingRepository mappingRepository;
    private final EvidenceMapper mapper;
    private final EvidenceAuditService auditService;
    private final DomainEventPublisher eventPublisher;
    private final CurrentUserProvider currentUser;

    public MappingService(NbaEvidenceRepository evidenceRepository,
                          NbaEvidenceMappingRepository mappingRepository,
                          EvidenceMapper mapper,
                          EvidenceAuditService auditService,
                          DomainEventPublisher eventPublisher,
                          CurrentUserProvider currentUser) {
        this.evidenceRepository = evidenceRepository;
        this.mappingRepository = mappingRepository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
    }

    @Transactional
    public MappingResponse addMapping(UUID evidenceId, CreateMappingRequest request) {
        requireEvidence(evidenceId);
        if (mappingRepository.existsByEvidenceIdAndCriterionIdAndRequirementId(
                evidenceId, request.criterionId(), request.requirementId())) {
            throw new DuplicateMappingException(
                    "Evidence is already mapped to this criterion/requirement");
        }
        MappingType type = request.mappingType() != null ? request.mappingType() : MappingType.SUPPORTING;
        NbaEvidenceMapping mapping = NbaEvidenceMapping.builder()
                .id(UUID.randomUUID())
                .evidenceId(evidenceId)
                .criterionId(request.criterionId())
                .requirementId(request.requirementId())
                .mappingType(type)
                .createdBy(currentUser.currentUserId())
                .build();
        mappingRepository.save(mapping);

        auditService.record(evidenceId, AuditAction.EVIDENCE_MAPPED, "NbaEvidenceMapping",
                mapping.getId(), null,
                "criterion=" + request.criterionId() + "; requirement=" + request.requirementId(), null);
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.MAPPED, evidenceId,
                currentUser.currentUserId(),
                Map.of("criterionId", request.criterionId(), "requirementId", request.requirementId())));
        return mapper.toMappingResponse(mapping);
    }

    @Transactional(readOnly = true)
    public List<MappingResponse> listMappings(UUID evidenceId) {
        requireEvidence(evidenceId);
        return mappingRepository.findByEvidenceId(evidenceId).stream()
                .map(mapper::toMappingResponse)
                .toList();
    }

    @Transactional
    public void deleteMapping(UUID evidenceId, UUID mappingId) {
        NbaEvidenceMapping mapping = mappingRepository.findByIdAndEvidenceId(mappingId, evidenceId)
                .orElseThrow(() -> new EvidenceNotFoundException(
                        "Mapping " + mappingId + " not found for evidence " + evidenceId));
        mappingRepository.delete(mapping);

        auditService.record(evidenceId, AuditAction.EVIDENCE_UNMAPPED, "NbaEvidenceMapping",
                mappingId, "criterion=" + mapping.getCriterionId()
                        + "; requirement=" + mapping.getRequirementId(), null, null);
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.UNMAPPED, evidenceId,
                currentUser.currentUserId(), Map.of("mappingId", mappingId)));
    }

    private void requireEvidence(UUID evidenceId) {
        if (!evidenceRepository.existsById(evidenceId)) {
            throw new EvidenceNotFoundException(evidenceId);
        }
    }
}
