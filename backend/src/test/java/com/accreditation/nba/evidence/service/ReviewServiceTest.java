package com.accreditation.nba.evidence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.accreditation.nba.evidence.dto.response.ReviewResponse;
import com.accreditation.nba.evidence.entity.NbaEvidence;
import com.accreditation.nba.evidence.entity.NbaEvidenceReview;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.enums.ReviewDecision;
import com.accreditation.nba.evidence.audit.EvidenceAuditService;
import com.accreditation.nba.evidence.event.DomainEventPublisher;
import com.accreditation.nba.evidence.exception.BadRequestException;
import com.accreditation.nba.evidence.exception.InvalidStateTransitionException;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.repository.NbaEvidenceRepository;
import com.accreditation.nba.evidence.repository.NbaEvidenceReviewRepository;
import com.accreditation.nba.evidence.workflow.EvidenceWorkflow;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private NbaEvidenceRepository evidenceRepository;
    @Mock
    private NbaEvidenceReviewRepository reviewRepository;
    @Mock
    private EvidenceAuditService auditService;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private CurrentUserProvider currentUser;

    private ReviewService reviewService;

    private final UUID evidenceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(evidenceRepository, reviewRepository, new EvidenceMapper(),
                new EvidenceWorkflow(), auditService, eventPublisher, currentUser);
    }

    private NbaEvidence evidenceWithStatus(EvidenceStatus status) {
        return NbaEvidence.builder()
                .id(evidenceId)
                .title("Doc")
                .status(status)
                .currentVersion(1)
                .uploadedBy("mock-user")
                .build();
    }

    @Test
    void approveMovesEvidenceToApproved() {
        when(evidenceRepository.findById(evidenceId))
                .thenReturn(Optional.of(evidenceWithStatus(EvidenceStatus.UNDER_REVIEW)));
        when(reviewRepository.save(any(NbaEvidenceReview.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(currentUser.currentUserId()).thenReturn("reviewer-1");

        ReviewResponse response = reviewService.review(evidenceId, ReviewDecision.APPROVE, "dr-smith", "Looks good");

        assertThat(response.decision()).isEqualTo(ReviewDecision.APPROVE);
        assertThat(response.resultingStatus()).isEqualTo(EvidenceStatus.APPROVED);
        assertThat(response.reviewer()).isEqualTo("dr-smith");
        verify(evidenceRepository).save(any(NbaEvidence.class));
    }

    @Test
    void rejectRequiresComments() {
        when(evidenceRepository.findById(evidenceId))
                .thenReturn(Optional.of(evidenceWithStatus(EvidenceStatus.UNDER_REVIEW)));

        assertThatThrownBy(() -> reviewService.review(evidenceId, ReviewDecision.REJECT, "dr-smith", "  "))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void cannotReviewWhenNotUnderReview() {
        when(evidenceRepository.findById(evidenceId))
                .thenReturn(Optional.of(evidenceWithStatus(EvidenceStatus.DRAFT)));

        assertThatThrownBy(() -> reviewService.review(evidenceId, ReviewDecision.APPROVE, "dr-smith", null))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
