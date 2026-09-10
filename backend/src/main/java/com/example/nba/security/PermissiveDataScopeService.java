package com.example.nba.security;

import java.util.UUID;

/**
 * Default no-op scope implementation used standalone. Permission checks
 * ({@code @PreAuthorize}) still apply; only fine-grained tenant scoping is a no-op.
 *
 * <p>Registered as a fallback {@code @Bean} in {@link DataScopeConfig} via
 * {@code @ConditionalOnMissingBean}, so the central RBAC team can drop in a real
 * {@link DataScopeService} bean/component and this one steps aside automatically.</p>
 */
public class PermissiveDataScopeService implements DataScopeService {

    @Override
    public void assertInstitutionAccess(UUID institutionId) {
        // no-op in standalone mode
    }

    @Override
    public void assertDepartmentAccess(UUID departmentId) {
        // no-op in standalone mode
    }

    @Override
    public void assertProgramAccess(UUID programId) {
        // no-op in standalone mode
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }
}
