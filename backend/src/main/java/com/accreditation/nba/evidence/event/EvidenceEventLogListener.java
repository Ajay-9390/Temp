package com.accreditation.nba.evidence.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Notification hook stub. Logs each domain event; represents the seam where the platform
 * Notification module (or a Kafka bridge) will later subscribe. This module intentionally
 * does NOT implement the full notification system.
 */
@Slf4j
@Component
public class EvidenceEventLogListener {

    @EventListener
    public void onEvidenceEvent(EvidenceDomainEvent event) {
        log.info("[evidence-event] type={} topic={} evidenceId={} actor={} payload={}",
                event.type(), event.type().topic(), event.evidenceId(), event.actor(), event.payload());
    }
}
