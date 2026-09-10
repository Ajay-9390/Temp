# Permission Contract

This module is **permission-based**, never role-hardcoded. The central RBAC team maps roles
to these permissions in Keycloak; the module only checks the permission (authority) string.

## Permissions defined by this module

| Domain | View | Create | Update | Status update |
|---|---|---|---|---|
| Institution | `INSTITUTION_VIEW` | `INSTITUTION_CREATE` | `INSTITUTION_UPDATE` | `INSTITUTION_STATUS_UPDATE` |
| Department | `DEPARTMENT_VIEW` | `DEPARTMENT_CREATE` | `DEPARTMENT_UPDATE` | `DEPARTMENT_STATUS_UPDATE` |
| Program | `PROGRAM_VIEW` | `PROGRAM_CREATE` | `PROGRAM_UPDATE` | `PROGRAM_STATUS_UPDATE` |
| Accreditation Cycle | `ACCREDITATION_CYCLE_VIEW` | `ACCREDITATION_CYCLE_CREATE` | `ACCREDITATION_CYCLE_UPDATE` | `ACCREDITATION_CYCLE_STATUS_UPDATE` |
| Academic Year | `ACADEMIC_YEAR_VIEW` | `ACADEMIC_YEAR_CREATE` | `ACADEMIC_YEAR_UPDATE` | `ACADEMIC_YEAR_STATUS_UPDATE` |
| Semester | `SEMESTER_VIEW` | `SEMESTER_CREATE` | `SEMESTER_UPDATE` | `SEMESTER_STATUS_UPDATE` |

## Suggested role → permission mapping (RBAC team decides final)

| Role | Typical scope | Suggested permissions |
|---|---|---|
| Admin | Institution-wide | all |
| NBA Coordinator | Program-wide | all except institution create/status |
| HOD | Department-wide | department + program + accreditation + academic (view/update) within department |
| Criterion / OBE Coordinator | Program context | mostly `*_VIEW`, selected updates |
| Faculty | Program context | `*_VIEW` |
| IQAC Reviewer | Institution/program | `*_VIEW`, some status updates |
| Auditor | Assigned cycle | `*_VIEW` only (read-only) |

## JWT expectations

The `KeycloakJwtAuthenticationConverter` accepts permissions from any of:
- `permissions`: `["PROGRAM_VIEW", ...]` (preferred)
- `realm_access.roles`: `["PROGRAM_VIEW", ...]`
- `resource_access.<client>.roles`: `["PROGRAM_VIEW", ...]`

A Keycloak role literally named `PROGRAM_CREATE` therefore satisfies
`@PreAuthorize("hasAuthority('PROGRAM_CREATE')")` with no code change.

## Multi-tenant scoping

Fine-grained scoping (which institution/department/program a principal may touch) is delegated
to the `DataScopeService` SPI so the RBAC team can implement tenant rules without touching
domain logic. See `docs/INTEGRATION.md` §9.
