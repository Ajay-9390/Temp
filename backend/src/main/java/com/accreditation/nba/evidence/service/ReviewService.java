package com.accreditation.nba.evidence.service;

import com.accreditation.nba.evidence.dto.response.ReviewResponse;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.entity.NbaEvidenceReview;
import com.accreditation.nba.evidence.enums.AuditAction;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.enums.ReviewDecision;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.event.DomainEventPublisher;
import com.accreditation.nba.evidence.event.EvidenceDomainEvent;
import com.accreditation.nba.evidence.event.EvidenceEventType;
import com.accreditation.nba.evidence.exception.BadRequestException;
import com.accreditation.nba.evidence.exception.EvidenceNotFoundException;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceReviewRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Records reviewer decisions (approve / reject / request-changes) and applies the resulting
 * workflow transition. Comments are mandatory for reject and request-changes. All prior
 * reviews are retained as history.
 */
@Service
public class ReviewService {

    private final NbaEvidenceRepository evidenceRepository;
    private final NbaEvidenceReviewRepository reviewRepository;
    private final EvidenceMapper mapper;
    private final com.accreditation.nba.evidence.workflow.EvidenceWorkflow workflow;
    private final EvidenceAuditService auditService;
    private final DomainEventPublisher eventPublisher;
    private final CurrentUserProvider currentUser;

    public ReviewService(NbaEvidenceRepository evidenceRepository,
                         NbaEvidenceReviewRepository reviewRepository,
                         EvidenceMapper mapper,
                         com.accreditation.nba.evidence.workflow.EvidenceWorkflow workflow,
                         EvidenceAuditService auditService,
                         DomainEventPublisher eventPublisher,
                         CurrentUserProvider currentUser) {
        this.evidenceRepository = evidenceRepository;
        this.reviewRepository = reviewRepository;
        this.mapper = mapper;
        this.workflow = workflow;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.currentUser = currentUser;
    }

    @Transactional
    public ReviewResponse review(UUID evidenceId, ReviewDecision decision, String reviewer, String comments) {
        NbaEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new EvidenceNotFoundException(evidenceId));

        EvidenceStatus target = decision.resultingStatus();
        // Reviews are only valid from UNDER_REVIEW; the workflow enforces this.
        workflow.assertCanTransition(evidence.getStatus(), target);

        if (decision.isCommentRequired() && (comments == null || comments.isBlank())) {
            throw new BadRequestException("Comments are required to " + decision.name().toLowerCase());
        }

        String actor = (reviewer != null && !reviewer.isBlank()) ? reviewer.trim() : currentUser.currentUserId();
        EvidenceStatus previous = evidence.getStatus();

        NbaEvidenceReview reviewRecord = NbaEvidenceReview.builder()
                .id(UUID.randomUUID())
                .evidenceId(evidenceId)
                .versionNumber(evidence.getCurrentVersion())
                .reviewer(actor)
                .decision(decision)
                .resultingStatus(target)
                .comments(comments)
                .build();
        reviewRepository.save(reviewRecord);

        evidence.setStatus(target);
        evidenceRepository.save(evidence);

        auditService.recordEvidence(evidenceId, auditActionFor(decision), previous.name(), target.name(), comments);
        eventPublisher.publish(EvidenceDomainEvent.of(eventTypeFor(decision), evidenceId, actor,
                Map.of("decision", decision.name(), "resultingStatus", target.name())));
        return mapper.toReviewResponse(reviewRecord);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviews(UUID evidenceId) {
        if (!evidenceRepository.existsById(evidenceId)) {
            throw new EvidenceNotFoundException(evidenceId);
        }
        return reviewRepository.findByEvidenceIdOrderByReviewedAtDesc(evidenceId).stream()
                .map(mapper::toReviewResponse)
                .toList();
    }

    private AuditAction auditActionFor(ReviewDecision decision) {
        return switch (decision) {
            case APPROVE -> AuditAction.EVIDENCE_APPROVED;
            case REJECT -> AuditAction.EVIDENCE_REJECTED;
            case REQUEST_CHANGES -> AuditAction.EVIDENCE_CHANGES_REQUESTED;
        };
    }

    private EvidenceEventType eventTypeFor(ReviewDecision decision) {
        return switch (decision) {
            case APPROVE -> EvidenceEventType.APPROVED;
            case REJECT -> EvidenceEventType.REJECTED;
            case REQUEST_CHANGES -> EvidenceEventType.CHANGES_REQUESTED;
        };
    }
}
