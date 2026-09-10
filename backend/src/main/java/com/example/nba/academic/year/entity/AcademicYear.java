package com.example.nba.academic.year.entity;

import com.example.nba.academic.AcademicLifecycleStatus;
import com.example.nba.common.audit.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.util.UUID;

/**
 * An academic year for a program (e.g. "2026-27"). Name is unique within a program.
 */
@Entity
@Table(name = "academic_year",
        uniqueConstraints = @UniqueConstraint(name = "uq_academic_year_program_name",
                columnNames = {"program_id", "name"}))
@Audited
@Getter
@Setter
public class AcademicYear extends BaseAuditEntity {

    @Column(name = "program_id", nullable = false)
    private UUID programId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademicLifecycleStatus status = AcademicLifecycleStatus.PLANNED;
}
