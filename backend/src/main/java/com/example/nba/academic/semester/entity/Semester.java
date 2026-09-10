package com.example.nba.academic.semester.entity;

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
 * A semester within an academic year. Semester number is unique within an academic year;
 * dates must fall within the parent academic year's date range (validated in the service).
 */
@Entity
@Table(name = "semester",
        uniqueConstraints = @UniqueConstraint(name = "uq_semester_year_number",
                columnNames = {"academic_year_id", "semester_number"}))
@Audited
@Getter
@Setter
public class Semester extends BaseAuditEntity {

    @Column(name = "academic_year_id", nullable = false)
    private UUID academicYearId;

    @Column(name = "semester_number", nullable = false)
    private Integer semesterNumber;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AcademicLifecycleStatus status = AcademicLifecycleStatus.PLANNED;
}
