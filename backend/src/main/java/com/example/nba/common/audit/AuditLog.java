package com.example.nba.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Custom business audit record (complements Envers row-level history). Captures who did
 * what to which entity, with optional before/after JSON and the request correlation id.
 */
@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    @Column(name = "user_id", length = 128)
    private String userId;

    @Column(name = "username", length = 128)
    private String username;

    @Column(name = "entity", nullable = false, length = 64)
    private String entity;

    @Column(name = "entity_id", nullable = false)
    private String entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private AuditAction action;

    @Column(name = "old_value", columnDefinition = "text")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "text")
    private String newValue;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}
