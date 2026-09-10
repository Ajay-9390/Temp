package com.example.nba.department.entity;

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
 * An academic department belonging to an institution.
 *
 * <p>Foreign keys are stored as plain UUID columns (not JPA associations) to keep module
 * boundaries decoupled — future modules and the RBAC team reference these ids directly.
 * {@code hodUserId} is an external reference into the future User/RBAC module; no User
 * entity is duplicated here.</p>
 */
@Entity
@Table(name = "department",
        uniqueConstraints = @UniqueConstraint(name = "uq_department_institution_code",
                columnNames = {"institution_id", "code"}))
@Audited
@Getter
@Setter
public class Department extends BaseAuditEntity {

    @Column(name = "institution_id", nullable = false)
    private UUID institutionId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 1000)
    private String description;

    /** External reference to a Keycloak/RBAC user; not a local FK. */
    @Column(name = "hod_user_id", length = 128)
    private String hodUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DepartmentStatus status = DepartmentStatus.ACTIVE;
}
