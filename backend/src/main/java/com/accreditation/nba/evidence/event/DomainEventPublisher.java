package com.accreditation.nba.evidence.event;

/**
 * Abstraction over event publishing so business code is decoupled from the transport. The
 * default {@link SpringDomainEventPublisher} publishes in-process Spring events; a future
 * {@code KafkaDomainEventPublisher} can replace it with no changes to services.
 */
public interface DomainEventPublisher {

    void publish(EvidenceDomainEvent event);
}
