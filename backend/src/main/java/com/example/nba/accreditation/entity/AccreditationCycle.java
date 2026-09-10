package com.example.nba.accreditation.entity;

import com.example.nba.common.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/**
 * An accreditation attempt/cycle for a program.
 *
 * <p>{@code tier} is stored as a free-form string (e.g. {@code TIER_I}, {@code TIER_II}) so
 * additional tiers can be added later without code changes; allowed values are validated in
 * the service against configurable metadata. {@code frameworkVersion} (e.g. "GAPC v4.0") is
 * stored separately. No NBA scoring/criteria rules live here.</p>
 */
@Entity
@Table(name = "accreditation_cycle")
@Audited
@Getter
@Setter
public class AccreditationCycle extends BaseAuditEntity {

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String tier;

    @Column(name = "framework_version", nullable = false, length = 100)
    private String frameworkVersion;

    @Column(name = "application_year", nullable = false)
    private Integer applicationYear;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccreditationCycleStatus status = AccreditationCycleStatus.DRAFT;

    @Column(length = 2000)
    private String remarks;
}
