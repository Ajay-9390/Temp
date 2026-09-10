package com.accreditation.nba.evidence.event;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Immutable business event envelope. Published in-process today (Spring events); the same
 * shape can be serialised to a Kafka topic later ({@link EvidenceEventType#topic()}).
 */
public record EvidenceDomainEvent(
        EvidenceEventType type,
        UUID evidenceId,
        String actor,
        OffsetDateTime occurredAt,
        Map<String, Object> payload
) {

    public static EvidenceDomainEvent of(EvidenceEventType type, UUID evidenceId, String actor,
                                         Map<String, Object> payload) {
        return new EvidenceDomainEvent(type, evidenceId, actor, OffsetDateTime.now(),
                payload == null ? Map.of() : payload);
    }

    public static EvidenceDomainEvent of(EvidenceEventType type, UUID evidenceId, String actor) {
        return of(type, evidenceId, actor, Map.of());
    }
}
