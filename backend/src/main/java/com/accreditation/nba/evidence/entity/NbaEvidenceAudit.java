package com.accreditation.nba.evidence.entity;

import com.accreditation.nba.evidence.enums.AuditAction;
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

/**
 * Custom, evidence-scoped business audit trail (table {@code nba_evidence_audit}).
 *
 * <p>This complements Hibernate Envers field-level history with meaningful business events
 * (who / what action / on which entity / old &rarr; new value / reason / when). It is
 * intentionally scoped to evidence; the platform-wide audit module is owned by another team.
 * This entity is NOT itself audited by Envers (it is the audit record).
 */
@Entity
@Table(name = "nba_evidence_audit", indexes = {
        @Index(name = "idx_audit_evidence", columnList = "evidence_id"),
        @Index(name = "idx_audit_action", columnList = "action")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NbaEvidenceAudit {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "evidence_id", nullable = false)
    private UUID evidenceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "performed_by", nullable = false, length = 120)
    private String performedBy;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "reason", columnDefinition = "text")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
