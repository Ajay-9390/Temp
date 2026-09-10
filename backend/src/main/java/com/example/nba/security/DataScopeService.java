package com.example.nba.security;

import java.util.UUID;

/**
 * Multi-tenant data-scoping SPI. The platform is multi-tenant
 * (Institution &rarr; Department &rarr; Program); a user may only access data within their
 * permitted scope (Admin = institution-wide, HOD = department-wide, NBA Coordinator =
 * program-wide, Auditor = assigned cycle, ...).
 *
 * <p>This module deliberately does <b>not</b> hardcode those scopes. It calls this interface
 * at read/write boundaries. A permissive {@link PermissiveDataScopeService} is provided for
 * standalone dev; the central RBAC team supplies the real implementation later (as a Spring
 * bean marked {@code @Primary}) without touching domain services.</p>
 */
public interface DataScopeService {

    /** Throws {@code AccessDeniedException} if the current principal may not touch this institution. */
    void assertInstitutionAccess(UUID institutionId);

    /** Throws {@code AccessDeniedException} if the current principal may not touch this department. */
    void assertDepartmentAccess(UUID departmentId);

    /** Throws {@code AccessDeniedException} if the current principal may not touch this program. */
    void assertProgramAccess(UUID programId);

    /** @return true if the current principal is restricted to read-only (e.g. Auditor role). */
    boolean isReadOnly();
}
