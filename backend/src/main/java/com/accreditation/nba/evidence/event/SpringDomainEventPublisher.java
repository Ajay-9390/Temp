package com.accreditation.nba.evidence.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * In-process publisher backed by Spring's {@link ApplicationEventPublisher}. Listeners in
 * this module (and, later, an outbox/Kafka bridge) can consume {@link EvidenceDomainEvent}s.
 */
@Component
public class SpringDomainEventPublisher implements DomainEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(EvidenceDomainEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
