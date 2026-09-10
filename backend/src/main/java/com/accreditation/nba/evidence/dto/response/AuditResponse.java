package com.accreditation.nba.evidence.dto.response;

import com.accreditation.nba.evidence.enums.AuditAction;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditResponse(
        UUID id,
        UUID evidenceId,
        AuditAction action,
        String entityType,
        UUID entityId,
        String performedBy,
        String oldValue,
        String newValue,
        String reason,
        OffsetDateTime createdAt
) {
}
