package com.accreditation.nba.evidence.audit;

import com.accreditation.nba.evidence.dto.response.AuditResponse;
import com.accreditation.nba.evidence.entity.NbaEvidenceAudit;
import com.accreditation.nba.evidence.enums.AuditAction;
import com.accreditation.nba.evidence.integration.CurrentUserProvider;
import com.accreditation.nba.evidence.mapper.EvidenceMapper;
import com.accreditation.nba.evidence.repository.NbaEvidenceAuditRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Writes evidence-scoped business audit events to {@code nba_evidence_audit}. Complements
 * Hibernate Envers field-level history with meaningful, queryable business actions. Scoped to
 * this module only — the platform-wide audit module is owned by another team.
 *
 * <p>Runs in the caller's transaction ({@code REQUIRED}) so an audit row is committed atomically
 * with the state change it records.
 */
@Service
public class EvidenceAuditService {

    private final NbaEvidenceAuditRepository auditRepository;
    private final CurrentUserProvider currentUserProvider;
    private final EvidenceMapper mapper;

    public EvidenceAuditService(NbaEvidenceAuditRepository auditRepository,
                                CurrentUserProvider currentUserProvider,
                                EvidenceMapper mapper) {
        this.auditRepository = auditRepository;
        this.currentUserProvider = currentUserProvider;
        this.mapper = mapper;
    }

    /** Full evidence-scoped business audit trail, newest first. */
    @Transactional(readOnly = true)
    public List<AuditResponse> getAuditTrail(UUID evidenceId) {
        return auditRepository.findByEvidenceIdOrderByCreatedAtDesc(evidenceId).stream()
                .map(mapper::toAuditResponse)
                .toList();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void record(UUID evidenceId, AuditAction action, String entityType, UUID entityId,
                       String oldValue, String newValue, String reason) {
        NbaEvidenceAudit audit = NbaEvidenceAudit.builder()
                .id(UUID.randomUUID())
                .evidenceId(evidenceId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .performedBy(currentUserProvider.currentUserId())
                .oldValue(oldValue)
                .newValue(newValue)
                .reason(reason)
                .build();
        auditRepository.save(audit);
    }

    /** Convenience for evidence-level actions (entityType = "NbaEvidence", entityId = evidenceId). */
    @Transactional(propagation = Propagation.REQUIRED)
    public void recordEvidence(UUID evidenceId, AuditAction action, String oldValue, String newValue,
                               String reason) {
        record(evidenceId, action, "NbaEvidence", evidenceId, oldValue, newValue, reason);
    }
}
