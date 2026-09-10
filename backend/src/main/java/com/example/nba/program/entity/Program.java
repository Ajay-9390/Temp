package com.example.nba.program.entity;

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

import java.util.UUID;

/**
 * An academic program (e.g. B.Tech CSE) belonging to a department.
 * Program code is unique within a department. Uses soft status transitions rather than
 * physical deletion.
 */
@Entity
@Table(name = "program",
        uniqueConstraints = @UniqueConstraint(name = "uq_program_department_code",
                columnNames = {"department_id", "code"}))
@Audited
@Getter
@Setter
public class Program extends BaseAuditEntity {

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 100)
    private String degree;

    @Column(length = 150)
    private String branch;

    @Column(length = 2000)
    private String description;

    @Column(name = "duration_years", nullable = false)
    private Integer durationYears;

    @Column(name = "total_semesters", nullable = false)
    private Integer totalSemesters;

    @Column(nullable = false)
    private Integer intake;

    @Column(name = "established_year")
    private Integer establishedYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProgramStatus status = ProgramStatus.DRAFT;
}
