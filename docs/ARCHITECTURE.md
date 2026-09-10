# NBA Program Management Module — Architecture

> Part of the larger NBA (National Board of Accreditation) accreditation automation
> platform. This repository contains **only** the Program Management module, built as a
> self-contained slice of a modular monolith so it can run independently today and be
> merged into the platform later without restructuring.

---

## 1. Requirement Analysis (Summary)

The module manages the **academic + accreditation context** of an NBA program:

```
Institution → Department → Program → AccreditationCycle
                                   → AcademicYear → Semester
```

It provides CRUD, lifecycle state machines, validation, permission-based authorization
(Keycloak/OIDC), audit trails, REST APIs, OpenAPI docs, a Next.js UI, tests and Docker.

It deliberately does **not** implement Outcomes, Attainment, CQI, Evidence, SAR, AI/RAG,
Kafka, or Camunda. It only exposes **stable UUID identifiers** and clean contracts so
those future modules can integrate.

---

## 2. Final Architecture

- **Modular monolith** (single Spring Boot deployable). Each domain (`institution`,
  `department`, `program`, `accreditation`, `academic`) is an internal package/module with
  its own controller/service/repository/entity/dto/mapper. Boundaries are enforced by
  package discipline, not by network calls.
- **Clean layered architecture**: `Controller → Service → Repository → Entity → PostgreSQL`.
- **DTO boundary**: controllers never expose JPA entities. Request DTOs in, Response DTOs
  out, MapStruct mappers between.
- **Cross-cutting concerns** live in `common/`: standardized `ApiResponse`, global
  exception handling, correlation-ID logging, audit, and an authorization/scoping SPI.
- **Security**: Spring Security resource server validating Keycloak JWTs. Authorization is
  **permission-based** (`@PreAuthorize("hasAuthority('PROGRAM_CREATE')")`), never
  role-hardcoded. A `DataScopeService` interface is the seam the central RBAC team fills in.
- **Extensibility seams** (present but intentionally minimal now):
  - `@Async` config disabled-by-default for future background jobs.
  - `LifecycleService` state machines that Camunda can later replace.
  - Redis cache abstraction on stable reads only.

```
        ┌─────────────┐    JWT (OIDC)   ┌──────────────┐
        │  Next.js UI │ ───────────────▶│   Keycloak   │
        └─────┬───────┘                 └──────────────┘
              │ REST /api/v1 (Bearer JWT)
        ┌─────▼─────────────────────────────────────────┐
        │        Spring Boot (modular monolith)          │
        │  Controller → Service → Repository → Entity    │
        │  common: response | exception | audit | scope  │
        └───────┬───────────────┬──────────────┬─────────┘
                │               │              │
          ┌─────▼────┐   ┌──────▼─────┐  ┌─────▼──────┐
          │ Postgres │   │   Redis    │  │  Actuator  │
          │ (Flyway) │   │  (cache)   │  │ /Prometheus│
          └──────────┘   └────────────┘  └────────────┘
```

---

## 3. ER Model

```
Institution (1) ──────< (N) Department
   PK id (uuid)               PK id (uuid)
   UQ code                    FK institution_id
                              UQ (institution_id, code)
                                    │
                                    │ (1)
                                    ▼ (N)
                              Program
                              PK id (uuid)
                              FK department_id
                              UQ (department_id, code)
                                 │
             ┌───────────────────┼─────────────────────┐
             │ (1..N)                                    │ (1..N)
             ▼                                           ▼
      AccreditationCycle                          AcademicYear
      PK id (uuid)                                PK id (uuid)
      FK program_id                               FK program_id
                                                  UQ (program_id, name)
                                                     │ (1..N)
                                                     ▼
                                                  Semester
                                                  PK id (uuid)
                                                  FK academic_year_id
                                                  UQ (academic_year_id, semester_number)
```

All PKs are UUID. Historical accreditation data is never physically deleted — soft
status transitions (`ARCHIVED`, `INACTIVE`, `EXPIRED`, etc.) are used instead.

---

## 4. Status Enums & Lifecycle Transitions

**Institution.status**: `ACTIVE`, `INACTIVE`

**Department.status**: `ACTIVE`, `INACTIVE`

**Program.status**: `DRAFT`, `ACTIVE`, `INACTIVE`, `ARCHIVED`
```
DRAFT    → ACTIVE
ACTIVE   → INACTIVE
INACTIVE → ACTIVE, ARCHIVED
```

**AccreditationCycle.status**: `DRAFT`, `PREPARATION`, `SUBMITTED`, `UNDER_REVIEW`,
`ACCREDITED`, `EXPIRED`, `REJECTED`, `CANCELLED`
```
DRAFT        → PREPARATION, CANCELLED
PREPARATION  → SUBMITTED, CANCELLED
SUBMITTED    → UNDER_REVIEW, CANCELLED
UNDER_REVIEW → ACCREDITED, REJECTED
ACCREDITED   → EXPIRED
REJECTED     → PREPARATION   (re-attempt)
```

**AcademicYear.status / Semester.status**: `PLANNED`, `ACTIVE`, `COMPLETED`, `ARCHIVED`
```
PLANNED   → ACTIVE
ACTIVE    → COMPLETED
COMPLETED → ARCHIVED
```

Transitions are enforced by dedicated `*LifecycleService` classes; invalid transitions
raise `409 CONFLICT` / `INVALID_STATE_TRANSITION`.

---

## 5. API Surface (v1)

| Method | Path | Permission |
|---|---|---|
| GET | `/api/v1/institutions/{id}` | `INSTITUTION_VIEW` |
| POST | `/api/v1/institutions` | `INSTITUTION_CREATE` |
| GET | `/api/v1/institutions` | `INSTITUTION_VIEW` |
| PUT | `/api/v1/institutions/{id}` | `INSTITUTION_UPDATE` |
| PATCH | `/api/v1/institutions/{id}/status` | `INSTITUTION_STATUS_UPDATE` |
| POST | `/api/v1/departments` | `DEPARTMENT_CREATE` |
| GET | `/api/v1/departments` | `DEPARTMENT_VIEW` |
| GET | `/api/v1/departments/{id}` | `DEPARTMENT_VIEW` |
| PUT | `/api/v1/departments/{id}` | `DEPARTMENT_UPDATE` |
| PATCH | `/api/v1/departments/{id}/status` | `DEPARTMENT_STATUS_UPDATE` |
| GET | `/api/v1/departments/{id}/programs` | `PROGRAM_VIEW` |
| POST | `/api/v1/programs` | `PROGRAM_CREATE` |
| GET | `/api/v1/programs` | `PROGRAM_VIEW` |
| GET | `/api/v1/programs/{id}` | `PROGRAM_VIEW` |
| GET | `/api/v1/programs/{id}/overview` | `PROGRAM_VIEW` |
| PUT | `/api/v1/programs/{id}` | `PROGRAM_UPDATE` |
| PATCH | `/api/v1/programs/{id}/status` | `PROGRAM_STATUS_UPDATE` |
| POST | `/api/v1/programs/{programId}/accreditation-cycles` | `ACCREDITATION_CYCLE_CREATE` |
| GET | `/api/v1/programs/{programId}/accreditation-cycles` | `ACCREDITATION_CYCLE_VIEW` |
| GET | `/api/v1/accreditation-cycles/{id}` | `ACCREDITATION_CYCLE_VIEW` |
| PUT | `/api/v1/accreditation-cycles/{id}` | `ACCREDITATION_CYCLE_UPDATE` |
| PATCH | `/api/v1/accreditation-cycles/{id}/status` | `ACCREDITATION_CYCLE_STATUS_UPDATE` |
| POST | `/api/v1/programs/{programId}/academic-years` | `ACADEMIC_YEAR_CREATE` |
| GET | `/api/v1/programs/{programId}/academic-years` | `ACADEMIC_YEAR_VIEW` |
| GET | `/api/v1/academic-years/{id}` | `ACADEMIC_YEAR_VIEW` |
| PUT | `/api/v1/academic-years/{id}` | `ACADEMIC_YEAR_UPDATE` |
| PATCH | `/api/v1/academic-years/{id}/status` | `ACADEMIC_YEAR_STATUS_UPDATE` |
| POST | `/api/v1/academic-years/{academicYearId}/semesters` | `SEMESTER_CREATE` |
| GET | `/api/v1/academic-years/{academicYearId}/semesters` | `SEMESTER_VIEW` |
| GET | `/api/v1/semesters/{id}` | `SEMESTER_VIEW` |
| PUT | `/api/v1/semesters/{id}` | `SEMESTER_UPDATE` |
| PATCH | `/api/v1/semesters/{id}/status` | `SEMESTER_STATUS_UPDATE` |

List endpoints support `page`, `size`, `sort`, and entity-specific filters.

---

## 6. Standard API Envelope

Success:
```json
{ "success": true, "data": {}, "message": "…", "timestamp": "2026-09-09T10:00:00Z" }
```
Error:
```json
{ "success": false,
  "error": { "code": "PROGRAM_CODE_ALREADY_EXISTS", "message": "…", "details": [] },
  "timestamp": "2026-09-09T10:00:00Z" }
```

---

## 7. RBAC Integration Approach

- The module ships a `PermissionCatalog` (constants) and enforces `@PreAuthorize` with
  those authority strings.
- A `KeycloakJwtAuthenticationConverter` maps JWT claims
  (`realm_access.roles`, `resource_access.*.roles`, and a `permissions` claim) into
  Spring `GrantedAuthority` objects. When the central RBAC team maps roles → permissions in
  Keycloak, no code changes are needed here.
- Multi-tenant data scoping is exposed via the `DataScopeService` interface
  (`applyScope(...)`, `assertCanAccess(...)`). A permissive default implementation is
  provided for standalone dev; RBAC team swaps in the real one.
- The module owns **no** User/Role/Permission tables. `hodUserId`, `createdBy`, `updatedBy`
  are stored as external references (Keycloak subject IDs / usernames).

---

## 8. Package Structure (backend)

```
com.example.nba
├─ NbaProgramManagementApplication
├─ config/          (OpenAPI, Jackson, Async, Cache, Web)
├─ security/        (SecurityConfig, JwtConverter, PermissionCatalog, scope SPI)
├─ common/
│   ├─ response/    (ApiResponse, ApiError, PageResponse)
│   ├─ exception/   (domain exceptions + GlobalExceptionHandler)
│   ├─ audit/       (Auditable base, AuditLog, listener, Envers)
│   └─ logging/     (CorrelationIdFilter, RequestLoggingFilter)
├─ institution/     controller|service|repository|entity|dto|mapper
├─ department/      controller|service|repository|entity|dto|mapper
├─ program/         controller|service|repository|entity|dto|mapper|lifecycle|validation
├─ accreditation/   controller|service|repository|entity|dto|mapper|lifecycle
└─ academic/
    ├─ year/        controller|service|repository|entity|dto|mapper
    └─ semester/    controller|service|repository|entity|dto|mapper
```

## 9. Frontend routes

```
/                                       dashboard redirect
/programs                               list
/programs/new                           create
/programs/[id]                          details (tabs: Overview/Accreditation/Years/Semesters)
/programs/[id]/edit                     edit
/programs/[id]/accreditation-cycles     cycles list
/programs/[id]/accreditation-cycles/new create cycle
/programs/[id]/academic-years           years list
/academic-years/[id]/semesters          semesters list
/departments                            list
/departments/[id]                       details + programs
```

## 10. Docker architecture

`docker-compose.yml` runs: `postgres` (+pgvector image for platform compatibility),
`redis`, `keycloak`, `backend`, `frontend`, `nginx` reverse proxy, `prometheus`, `grafana`.
Secrets via `.env` (`.env.example` provided).
