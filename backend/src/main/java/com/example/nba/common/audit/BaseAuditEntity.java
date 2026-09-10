package com.example.nba.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all persistent entities in this module.
 *
 * <ul>
 *   <li>UUID primary key (application-generated for portability across the platform).</li>
 *   <li>Spring Data auditing for {@code createdAt/updatedAt/createdBy/updatedBy}.</li>
 *   <li>Hibernate Envers ({@code @Audited}) for full historical revisions.</li>
 * </ul>
 */
@Getter
@Setter
@MappedSuperclass
@Audited
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id = UUID.randomUUID();

    // Audit-metadata columns are excluded from Envers history (@NotAudited): Envers already
    // records its own revision timestamp/actor, so mirroring them would just bloat *_aud tables.
    @NotAudited
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @NotAudited
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @NotAudited
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 128)
    private String createdBy;

    @NotAudited
    @LastModifiedBy
    @Column(name = "updated_by", length = 128)
    private String updatedBy;

    /** Optimistic locking guards concurrent edits. */
    @NotAudited
    @jakarta.persistence.Version
    @Column(name = "version", nullable = false)
    private long version;
}
