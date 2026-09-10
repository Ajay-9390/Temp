package com.example.nba.common.audit;

import com.example.nba.common.logging.CorrelationIdFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Writes business audit records. Kept intentionally simple; callers pass the entity name,
 * id, action and optional old/new snapshots. Runs in its own transaction so audit is
 * recorded even if the surrounding transaction is examined for rollback semantics.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String entity, UUID entityId, AuditAction action, String oldValue, String newValue) {
        AuditLog log = new AuditLog();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
        log.setUserId(principal);
        log.setUsername(principal);
        log.setEntity(entity);
        log.setEntityId(entityId == null ? "n/a" : entityId.toString());
        log.setAction(action);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setCorrelationId(MDC.get(CorrelationIdFilter.MDC_CORRELATION));
        repository.save(log);
    }

    public void record(String entity, UUID entityId, AuditAction action) {
        record(entity, entityId, action, null, null);
    }
}
