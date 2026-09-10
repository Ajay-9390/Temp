package com.accreditation.nba.evidence.entity;

import com.accreditation.nba.evidence.enums.EvidenceStatus;
import com.accreditation.nba.evidence.enums.ReviewDecision;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

/**
 * A recorded review decision on an evidence item (table {@code nba_evidence_review}).
 * The full history of reviews is retained (approve / reject / request-changes).
 */
@Entity
@Table(name = "nba_evidence_review",
        indexes = @Index(name = "idx_review_evidence", columnList = "evidence_id"))
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NbaEvidenceReview {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    /** Version number that was reviewed. */
    @Column(name = "version_number")
    private Integer versionNumber;

    @Column(name = "reviewer", nullable = false, length = 120)
    private String reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false, length = 30)
    private ReviewDecision decision;

    @Enumerated(EnumType.STRING)
    @Column(name = "resulting_status", nullable = false, length = 40)
    private EvidenceStatus resultingStatus;

    @Column(name = "comments", columnDefinition = "text")
    private String comments;

    @CreationTimestamp
    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private OffsetDateTime reviewedAt;
}
