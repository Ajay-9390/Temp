package com.accreditation.nba.evidence.entity;

import com.accreditation.nba.evidence.enums.EvidenceCategory;
import com.accreditation.nba.evidence.enums.EvidenceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.envers.Audited;

/**
 * Root evidence aggregate (table {@code nba_evidence}).
 *
 * <p>External references ({@code programId}, {@code departmentId}, {@code academicYearId},
 * {@code criterionId}, {@code requirementId}) are plain UUID columns owned by other modules.
 * There are no cross-module foreign keys. Child records (versions, mappings, reviews, audit)
 * reference this row by {@code evidenceId} and are managed through their own repositories.
 */
@Entity
@Table(name = "nba_evidence", indexes = {
        @Index(name = "idx_evidence_program", columnList = "program_id"),
        @Index(name = "idx_evidence_academic_year", columnList = "academic_year_id"),
        @Index(name = "idx_evidence_criterion", columnList = "criterion_id"),
        @Index(name = "idx_evidence_status", columnList = "status"),
        @Index(name = "idx_evidence_category", columnList = "category")
})
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NbaEvidence {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 40)
    private EvidenceCategory category;

    // ---- External module references (IDs only, no FK) ----
    @Column(name = "program_id")
    private UUID programId;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "academic_year_id")
    private UUID academicYearId;

    @Column(name = "criterion_id")
    private UUID criterionId;

    @Column(name = "requirement_id")
    private UUID requirementId;
    // ------------------------------------------------------

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 40)
    private EvidenceStatus status;

    @Column(name = "current_version", nullable = false)
    private int currentVersion;

    @Convert(converter = StringListConverter.class)
    @Column(name = "tags", columnDefinition = "text")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    /**
     * Read-only mirror of the raw {@code tags} column (comma-separated) used for keyword
     * search in JPA Criteria queries, where the converted {@code List<String>} cannot be
     * treated as text. Never written directly.
     */
    @Column(name = "tags", insertable = false, updatable = false)
    private String tagsRaw;

    @Column(name = "uploaded_by", nullable = false, length = 120)
    private String uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
