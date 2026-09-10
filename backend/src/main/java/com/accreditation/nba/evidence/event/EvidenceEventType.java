package com.accreditation.nba.evidence.event;

/**
 * Business event types emitted by the module. Each maps to a suggested Kafka topic so the
 * in-process publisher can later be swapped for a Kafka publisher without touching services.
 * The Notification module (owned by another team) can subscribe to these.
 */
public enum EvidenceEventType {
    CREATED("nba.evidence.created"),
    UPDATED("nba.evidence.updated"),
    SUBMITTED("nba.evidence.submitted"),
    REVIEW_STARTED("nba.evidence.review-started"),
    REVIEWED("nba.evidence.reviewed"),
    APPROVED("nba.evidence.approved"),
    REJECTED("nba.evidence.rejected"),
    CHANGES_REQUESTED("nba.evidence.changes-requested"),
    VERSION_CREATED("nba.evidence.version-created"),
    MAPPED("nba.evidence.mapped"),
    UNMAPPED("nba.evidence.unmapped"),
    ARCHIVED("nba.evidence.archived"),
    DELETED("nba.evidence.deleted");

    private final String topic;

    EvidenceEventType(String topic) {
        this.topic = topic;
    }

    public String topic() {
        return topic;
    }
}
