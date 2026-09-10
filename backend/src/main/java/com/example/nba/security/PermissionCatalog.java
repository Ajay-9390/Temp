package com.example.nba.security;

/**
 * Canonical permission strings enforced by this module via {@code @PreAuthorize}.
 *
 * <p>These are the authority names expected inside the Keycloak JWT. The central RBAC team
 * maps roles (Admin, NBA Coordinator, HOD, Faculty, Auditor, ...) to these permissions;
 * this module never hardcodes role checks.</p>
 */
public final class PermissionCatalog {

    private PermissionCatalog() {
    }

    // Institution
    public static final String INSTITUTION_VIEW = "INSTITUTION_VIEW";
    public static final String INSTITUTION_CREATE = "INSTITUTION_CREATE";
    public static final String INSTITUTION_UPDATE = "INSTITUTION_UPDATE";
    public static final String INSTITUTION_STATUS_UPDATE = "INSTITUTION_STATUS_UPDATE";

    // Department
    public static final String DEPARTMENT_VIEW = "DEPARTMENT_VIEW";
    public static final String DEPARTMENT_CREATE = "DEPARTMENT_CREATE";
    public static final String DEPARTMENT_UPDATE = "DEPARTMENT_UPDATE";
    public static final String DEPARTMENT_STATUS_UPDATE = "DEPARTMENT_STATUS_UPDATE";

    // Program
    public static final String PROGRAM_VIEW = "PROGRAM_VIEW";
    public static final String PROGRAM_CREATE = "PROGRAM_CREATE";
    public static final String PROGRAM_UPDATE = "PROGRAM_UPDATE";
    public static final String PROGRAM_STATUS_UPDATE = "PROGRAM_STATUS_UPDATE";

    // Accreditation Cycle
    public static final String ACCREDITATION_CYCLE_VIEW = "ACCREDITATION_CYCLE_VIEW";
    public static final String ACCREDITATION_CYCLE_CREATE = "ACCREDITATION_CYCLE_CREATE";
    public static final String ACCREDITATION_CYCLE_UPDATE = "ACCREDITATION_CYCLE_UPDATE";
    public static final String ACCREDITATION_CYCLE_STATUS_UPDATE = "ACCREDITATION_CYCLE_STATUS_UPDATE";

    // Academic Year
    public static final String ACADEMIC_YEAR_VIEW = "ACADEMIC_YEAR_VIEW";
    public static final String ACADEMIC_YEAR_CREATE = "ACADEMIC_YEAR_CREATE";
    public static final String ACADEMIC_YEAR_UPDATE = "ACADEMIC_YEAR_UPDATE";
    public static final String ACADEMIC_YEAR_STATUS_UPDATE = "ACADEMIC_YEAR_STATUS_UPDATE";

    // Semester
    public static final String SEMESTER_VIEW = "SEMESTER_VIEW";
    public static final String SEMESTER_CREATE = "SEMESTER_CREATE";
    public static final String SEMESTER_UPDATE = "SEMESTER_UPDATE";
    public static final String SEMESTER_STATUS_UPDATE = "SEMESTER_STATUS_UPDATE";

    // Convenience constants for @PreAuthorize (SpEL string literals)
    public static final String HAS_INSTITUTION_VIEW = "hasAuthority('" + INSTITUTION_VIEW + "')";
    public static final String HAS_INSTITUTION_CREATE = "hasAuthority('" + INSTITUTION_CREATE + "')";
    public static final String HAS_INSTITUTION_UPDATE = "hasAuthority('" + INSTITUTION_UPDATE + "')";
    public static final String HAS_INSTITUTION_STATUS_UPDATE = "hasAuthority('" + INSTITUTION_STATUS_UPDATE + "')";

    public static final String HAS_DEPARTMENT_VIEW = "hasAuthority('" + DEPARTMENT_VIEW + "')";
    public static final String HAS_DEPARTMENT_CREATE = "hasAuthority('" + DEPARTMENT_CREATE + "')";
    public static final String HAS_DEPARTMENT_UPDATE = "hasAuthority('" + DEPARTMENT_UPDATE + "')";
    public static final String HAS_DEPARTMENT_STATUS_UPDATE = "hasAuthority('" + DEPARTMENT_STATUS_UPDATE + "')";

    public static final String HAS_PROGRAM_VIEW = "hasAuthority('" + PROGRAM_VIEW + "')";
    public static final String HAS_PROGRAM_CREATE = "hasAuthority('" + PROGRAM_CREATE + "')";
    public static final String HAS_PROGRAM_UPDATE = "hasAuthority('" + PROGRAM_UPDATE + "')";
    public static final String HAS_PROGRAM_STATUS_UPDATE = "hasAuthority('" + PROGRAM_STATUS_UPDATE + "')";

    public static final String HAS_CYCLE_VIEW = "hasAuthority('" + ACCREDITATION_CYCLE_VIEW + "')";
    public static final String HAS_CYCLE_CREATE = "hasAuthority('" + ACCREDITATION_CYCLE_CREATE + "')";
    public static final String HAS_CYCLE_UPDATE = "hasAuthority('" + ACCREDITATION_CYCLE_UPDATE + "')";
    public static final String HAS_CYCLE_STATUS_UPDATE = "hasAuthority('" + ACCREDITATION_CYCLE_STATUS_UPDATE + "')";

    public static final String HAS_YEAR_VIEW = "hasAuthority('" + ACADEMIC_YEAR_VIEW + "')";
    public static final String HAS_YEAR_CREATE = "hasAuthority('" + ACADEMIC_YEAR_CREATE + "')";
    public static final String HAS_YEAR_UPDATE = "hasAuthority('" + ACADEMIC_YEAR_UPDATE + "')";
    public static final String HAS_YEAR_STATUS_UPDATE = "hasAuthority('" + ACADEMIC_YEAR_STATUS_UPDATE + "')";

    public static final String HAS_SEMESTER_VIEW = "hasAuthority('" + SEMESTER_VIEW + "')";
    public static final String HAS_SEMESTER_CREATE = "hasAuthority('" + SEMESTER_CREATE + "')";
    public static final String HAS_SEMESTER_UPDATE = "hasAuthority('" + SEMESTER_UPDATE + "')";
    public static final String HAS_SEMESTER_STATUS_UPDATE = "hasAuthority('" + SEMESTER_STATUS_UPDATE + "')";
}
