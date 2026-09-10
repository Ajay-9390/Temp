package com.accreditation.nba.evidence.entity;

import com.accreditation.nba.evidence.enums.MappingType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
 * Links one evidence item to one NBA criterion/requirement (table {@code nba_evidence_mapping}).
 *
 * <p>One evidence item can support many requirements: the file is stored once and multiple
 * mapping rows are created. {@code criterionId}/{@code requirementId} are external references
 * owned by the NBA Criteria module.
 */
@Entity
@Table(name = "nba_evidence_mapping",
        uniqueConstraints = @UniqueConstraint(name = "uq_mapping_evidence_criterion_requirement",
                columnNames = {"evidence_id", "criterion_id", "requirement_id"}),
        indexes = {
                @Index(name = "idx_mapping_evidence", columnList = "evidence_id"),
                @Index(name = "idx_mapping_criterion", columnList = "criterion_id"),
                @Index(name = "idx_mapping_requirement", columnList = "requirement_id")
        })
@Audited
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NbaEvidenceMapping {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Column(name = "criterion_id", nullable = false)
    private UUID criterionId;

    @Column(name = "requirement_id", nullable = false)
    private UUID requirementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "mapping_type", nullable = false, length = 30)
    private MappingType mappingType;

    @Column(name = "created_by", length = 120)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
