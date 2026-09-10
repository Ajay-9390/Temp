package com.accreditation.nba.evidence.service;

import com.accreditation.nba.evidence.dto.request.CreateEvidenceRequest;
import com.accreditation.nba.evidence.dto.request.EvidenceSearchCriteria;
import com.accreditation.nba.evidence.dto.request.UpdateEvidenceRequest;
import com.accreditation.nba.evidence.dto.response.EvidenceResponse;
import com.accreditation.nba.evidence.dto.response.EvidenceSummaryResponse;
import com.accreditation.nba.evidence.dto.response.PageResponse;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.entity.NbaEvidenceVersion;
import com.accreditation.nba.evidence.enums.AuditAction;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.event.DomainEventPublisher;
import com.accreditation.nba.evidence.event.EvidenceDomainEvent;
import com.accreditation.nba.evidence.event.EvidenceEventType;
import com.accreditation.nba.evidence.exception.BadRequestException;
import com.accreditation.nba.evidence.exception.EvidenceNotFoundException;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.repository.NbaEvidenceMappingRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceReviewRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceVersionRepository;
import com.accreditation.nba.evidence.storage.EvidenceStorageService;
import com.accreditation.nba.evidence.workflow.EvidenceWorkflow;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Central service for the evidence aggregate: CRUD plus lifecycle transitions (submit,
 * start-review, archive). Enforces workflow rules, writes audit entries and publishes domain
 * events. Review decisions live in {@link ReviewService}; file versions in {@link VersionService}.
 */
@Slf4j
@Service
public class EvidenceService {

    private final NbaEvidenceRepository evidenceRepository;
    private final NbaEvidenceVersionRepository versionRepository;
    private final NbaEvidenceMappingRepository mappingRepository;
    private final NbaEvidenceReviewRepository reviewRepository;
    private final EvidenceMapper mapper;
    private final EvidenceWorkflow workflow;
    private final EvidenceAuditService auditService;
    private final DomainEventPublisher eventPublisher;
    private final CurrentUserProvider currentUser;
    private final VersionService versionService;
    private final SearchService searchService;
    private final EvidenceStorageService storageService;

    public EvidenceService(NbaEvidenceRepository evidenceRepository,
                           NbaEvidenceVersionRepository versionRepository,
                           NbaEvidenceMappingRepository mappingRepository,
                           NbaEvidenceReviewRepository reviewRepository,
                           EvidenceMapper mapper,
                           EvidenceWorkflow workflow,
                           EvidenceAuditService auditService,
                           DomainEventPublisher eventPublisher,
                           CurrentUserProvider currentUser,
                           VersionService versionService,
                           SearchService searchService,
                           EvidenceStorageService storageService) {
        this.evidenceRepository = evidenceRepository;
        this.versionRepository = versionRepository;
        this.mappingRepository = mappingRepository;
        this.reviewRepository = reviewRepository;
        this.mapper = mapper;
        this.workflow = workflow;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
        this.versionService = versionService;
        this.searchService = searchService;
        this.storageService = storageService;
    }

    @Transactional
    public EvidenceResponse create(CreateEvidenceRequest request, MultipartFile file) {
        NbaEvidence evidence = NbaEvidence.builder()
                .id(UUID.randomUUID())
                .title(request.title())
                .description(request.description())
                .category(request.category())
                .programId(request.programId())
                .departmentId(request.departmentId())
                .academicYearId(request.academicYearId())
                .criterionId(request.criterionId())
                .requirementId(request.requirementId())
                .status(EvidenceStatus.DRAFT)
                .currentVersion(0)
                .tags(request.tags() == null ? new ArrayList<>() : new ArrayList<>(request.tags()))
                .uploadedBy(currentUser.currentUserId())
                .build();
        evidenceRepository.save(evidence);

        auditService.recordEvidence(evidence.getId(), AuditAction.EVIDENCE_CREATED, null,
                evidence.getTitle(), "Evidence created");
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.CREATED, evidence.getId(),
                currentUser.currentUserId(), Map.of("title", evidence.getTitle())));

        if (file != null && !file.isEmpty()) {
            versionService.createVersion(evidence.getId(), file, "Initial version");
        }
        return buildResponse(reload(evidence.getId()));
    }

    @Transactional(readOnly = true)
    public EvidenceResponse get(UUID id) {
        return buildResponse(reload(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<EvidenceSummaryResponse> list(EvidenceSearchCriteria criteria, Pageable pageable) {
        Page<NbaEvidence> page = searchService.search(criteria, pageable);
        return PageResponse.from(page, e -> mapper.toSummary(e, latestFileType(e.getId())));
    }

    @Transactional
    public EvidenceResponse update(UUID id, UpdateEvidenceRequest request) {
        NbaEvidence evidence = reload(id);
        if (evidence.getStatus() == EvidenceStatus.ARCHIVED) {
            throw new BadRequestException("Cannot edit archived evidence");
        }
        String before = snapshot(evidence);
        evidence.setTitle(request.title());
        evidence.setDescription(request.description());
        evidence.setCategory(request.category());
        evidence.setProgramId(request.programId());
        evidence.setDepartmentId(request.departmentId());
        evidence.setAcademicYearId(request.academicYearId());
        evidence.setCriterionId(request.criterionId());
        evidence.setRequirementId(request.requirementId());
        evidence.setTags(request.tags() == null ? new ArrayList<>() : new ArrayList<>(request.tags()));
        evidenceRepository.save(evidence);

        auditService.recordEvidence(id, AuditAction.EVIDENCE_UPDATED, before, snapshot(evidence),
                "Metadata updated");
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.UPDATED, id,
                currentUser.currentUserId()));
        return buildResponse(evidence);
    }

    @Transactional
    public void delete(UUID id) {
        NbaEvidence evidence = reload(id);
        if (evidence.getStatus() != EvidenceStatus.DRAFT) {
            throw new BadRequestException(
                    "Only DRAFT evidence can be permanently deleted; archive it instead");
        }
        // Remove stored files (best-effort) before cascading the DB rows.
        versionRepository.findByEvidenceIdOrderByVersionNumberDesc(id).forEach(v -> {
            try {
                storageService.delete(v.getStoragePath());
            } catch (RuntimeException e) {
                log.warn("Failed to delete stored object {}: {}", v.getStoragePath(), e.getMessage());
            }
        });
        evidenceRepository.delete(evidence);
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.DELETED, id,
                currentUser.currentUserId()));
    }

    @Transactional
    public EvidenceResponse submit(UUID id, String comment) {
        NbaEvidence evidence = reload(id);
        if (evidence.getCurrentVersion() < 1) {
            throw new BadRequestException("Cannot submit evidence without an uploaded file");
        }
        transition(evidence, EvidenceStatus.SUBMITTED);
        auditService.recordEvidence(id, AuditAction.EVIDENCE_SUBMITTED, null, EvidenceStatus.SUBMITTED.name(), comment);
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.SUBMITTED, id, currentUser.currentUserId()));
        return buildResponse(evidence);
    }

    @Transactional
    public EvidenceResponse startReview(UUID id) {
        NbaEvidence evidence = reload(id);
        transition(evidence, EvidenceStatus.UNDER_REVIEW);
        auditService.recordEvidence(id, AuditAction.EVIDENCE_UPDATED, EvidenceStatus.SUBMITTED.name(),
                EvidenceStatus.UNDER_REVIEW.name(), "Review started");
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.REVIEW_STARTED, id, currentUser.currentUserId()));
        return buildResponse(evidence);
    }

    @Transactional
    public EvidenceResponse archive(UUID id, String reason) {
        NbaEvidence evidence = reload(id);
        String from = evidence.getStatus().name();
        transition(evidence, EvidenceStatus.ARCHIVED);
        auditService.recordEvidence(id, AuditAction.EVIDENCE_ARCHIVED, from, EvidenceStatus.ARCHIVED.name(), reason);
        eventPublisher.publish(EvidenceDomainEvent.of(EvidenceEventType.ARCHIVED, id, currentUser.currentUserId()));
        return buildResponse(evidence);
    }

    // ---- helpers ----

    private void transition(NbaEvidence evidence, EvidenceStatus target) {
        workflow.assertCanTransition(evidence.getStatus(), target);
        evidence.setStatus(target);
        evidenceRepository.save(evidence);
    }

    private EvidenceResponse buildResponse(NbaEvidence evidence) {
        UUID id = evidence.getId();
        long versions = versionRepository.countByEvidenceId(id);
        long mappings = mappingRepository.countByEvidenceId(id);
        long reviews = reviewRepository.countByEvidenceId(id);
        NbaEvidenceVersion latest = versionRepository
                .findTopByEvidenceIdOrderByVersionNumberDesc(id).orElse(null);
        return mapper.toEvidenceResponse(evidence, versions, mappings, reviews, latest);
    }

    private String latestFileType(UUID evidenceId) {
        return versionRepository.findTopByEvidenceIdOrderByVersionNumberDesc(evidenceId)
                .map(NbaEvidenceVersion::getFileType)
                .orElse(null);
    }

    private NbaEvidence reload(UUID id) {
        return evidenceRepository.findById(id)
                .orElseThrow(() -> new EvidenceNotFoundException(id));
    }

    private String snapshot(NbaEvidence e) {
        return "title=" + e.getTitle() + "; category=" + e.getCategory()
                + "; status=" + e.getStatus() + "; criterionId=" + e.getCriterionId()
                + "; requirementId=" + e.getRequirementId();
    }
}
