# NBA Evidence Management Module

A clean, independent module for the shared **NBA + NAAC Accreditation Management Platform**.
It manages the complete lifecycle of NBA accreditation evidence: creation, upload, validation,
storage, metadata extraction, mapping, review, approval/rejection, versioning, audit, search,
gap detection, statistics and reports.

> This module is developed independently (1 of 12 developers). It references other modules
> **by ID only** and never re-creates their tables. Authentication is intentionally **absent**
> in this development phase — a mock identity is used (`X-User-Id` / `X-User-Name` headers,
> defaulting to `mock-user`).

---

## Contents

- `backend/` — Java 21 · Spring Boot 3 · Spring Web/Data JPA · Hibernate · PostgreSQL · Flyway · OpenAPI
- `frontend/` — Next.js · TypeScript · Tailwind · shadcn/ui · TanStack Query · React Hook Form · Zod
- `docs/` — [DESIGN](docs/DESIGN.md) · [API contract](docs/API.md) · [Integration contracts](docs/INTEGRATION.md)
- `docker-compose.yml` — Postgres + Redis + backend + frontend

---

## Quick start (Docker)

```bash
cp .env.example .env         # adjust values; never commit .env
docker compose up --build
```

- Frontend: http://localhost:3000  → redirects to `/evidence`
- Backend API: http://localhost:8080/api/v1/evidences
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

The backend runs Flyway migrations on startup (creates only the `nba_evidence_*` tables).

## Run locally (without Docker)

**Backend** (needs Java 21 and a PostgreSQL database named `accreditation`):

```bash
cd backend
./mvnw spring-boot:run            # Windows: mvnw.cmd spring-boot:run
```

Configuration is environment-driven (see `backend/src/main/resources/application.yml`).
Defaults target `jdbc:postgresql://localhost:5432/accreditation` (user/pass `postgres`).
File storage defaults to the local filesystem (`./data/evidence-store`), so no external
service is required to start.

**Frontend** (needs Node 18+):

```bash
cd frontend
cp .env.local.example .env.local  # BACKEND_INTERNAL_URL=http://localhost:8080
npm install
npm run dev
```

The Next.js server proxies `/api/*` to the backend (see `next.config.mjs`), so the browser
uses relative URLs and there are no CORS concerns in dev.

---

## Architecture

Layered/clean architecture — controllers handle HTTP only, business rules live in services,
infrastructure (storage, validation, extraction, OCR, events, audit) sits behind interfaces.

```
Next.js (TanStack Query) ──▶ REST /api/v1/evidences ──▶ Services ──▶ Repositories ──▶ PostgreSQL (metadata)
                                                          │
                                                          ├─▶ EvidenceStorageService ─▶ Supabase / Local FS (files)
                                                          ├─▶ FileValidation (Tika, SHA-256)
                                                          ├─▶ Extraction (PDFBox) + async OCR (Tesseract)
                                                          ├─▶ EvidenceWorkflow (state machine)
                                                          ├─▶ Domain events (Spring; Kafka-ready)
                                                          └─▶ Audit (custom + Envers-ready)
```

See [docs/DESIGN.md](docs/DESIGN.md) for the ER diagram, lifecycle state machine and package layout.

### Evidence lifecycle

`DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED | CHANGES_REQUIRED`,
with `CHANGES_REQUIRED → SUBMITTED`, revisions from `APPROVED/REJECTED → DRAFT` (via a new
version), and `→ ARCHIVED`. Invalid transitions are rejected in the service layer.

---

## Owned tables (this module only)

`nba_evidence`, `nba_evidence_version`, `nba_evidence_mapping`, `nba_evidence_review`,
`nba_evidence_audit`. All IDs are UUID. External references (`program_id`, `department_id`,
`academic_year_id`, `criterion_id`, `requirement_id`) are FK-ready UUID columns with **no
cross-module foreign keys**.

## Integration contracts

- **Inbound**: program/department/academic-year/criterion/requirement IDs (owned by other
  modules). Gap detection consumes `EvidenceRequirementProvider` (mock implementation ships
  now; replace with the real NBA Criteria client later).
- **Identity**: `CurrentUserProvider` (mock) — the single seam for real auth (JWT/Keycloak).
- **Outbound events**: `EvidenceCreated/Submitted/Reviewed/Approved/Rejected/ChangesRequested/
  VersionCreated/Mapped/Unmapped` — in-process Spring events today, Kafka-ready.

Details in [docs/INTEGRATION.md](docs/INTEGRATION.md).

---

## API summary

Base: `/api/v1/evidences` (full contract in [docs/API.md](docs/API.md)).

| Area       | Endpoints |
|------------|-----------|
| CRUD       | `POST /`, `GET /`, `GET /{id}`, `PUT /{id}`, `DELETE /{id}` |
| Lifecycle  | `POST /{id}/submit`, `/start-review`, `/approve`, `/reject`, `/request-changes`, `/archive` |
| Versions   | `GET/POST /{id}/versions`, `GET /{id}/versions/{n}`, `/download`, `/preview` |
| Mappings   | `GET/POST /{id}/mappings`, `DELETE /{id}/mappings/{mappingId}` |
| Reviews    | `GET /{id}/reviews` |
| Audit      | `GET /{id}/audit` |
| Analytics  | `GET /statistics`, `GET /gaps` |
| Reports    | `GET /reports/completion`, `GET /reports/status` (`format=xlsx|pdf|csv`) |

## Frontend pages

`/evidence` (dashboard), `/evidence/list`, `/evidence/upload`, `/evidence/[id]` (details),
`/evidence/[id]/edit`, `/evidence/[id]/review`, `/evidence/[id]/versions`, `/evidence/gaps`,
`/evidence/reports`.

---

## Testing

**Backend**

```bash
cd backend
./mvnw test        # unit tests (fast, no Docker)
./mvnw verify      # + Testcontainers integration test (requires Docker)
```

**Frontend**

```bash
cd frontend
npm run typecheck
npm run build
npm run test:e2e   # Playwright (start backend + frontend first; run `npx playwright install` once)
```

---

## Key environment variables

| Variable | Default | Purpose |
|----------|---------|---------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/accreditation` | Shared DB |
| `STORAGE_PROVIDER` | `local` | `local` or `supabase` |
| `SUPABASE_URL` / `SUPABASE_SERVICE_KEY` / `SUPABASE_BUCKET` | — | Supabase Storage |
| `CACHE_TYPE` | `none` | `redis` to enable statistics cache |
| `OCR_ENABLED` | `false` | Enable Tesseract OCR (needs native tesseract) |
| `ENVERS_ENABLED` | `false` | Hibernate Envers field-level history |
| `DDL_AUTO` | `none` | Flyway owns schema; set `validate` to check drift |

See `.env.example` (Docker) and `backend/src/main/resources/application.yml`.

---

## Scope / boundaries

**Owns:** evidence, versions, file metadata, mappings, reviews, status, evidence audit,
search, gap data, statistics.

**Does NOT own / not implemented here:** Program, Department, Course, CO/PO/PSO, Curriculum,
Assessment, Attainment, NBA Criteria, SAR, Users/Roles/Auth, platform-wide Notification/Audit
modules, Kafka, Camunda, OpenSearch. These are referenced by ID or via integration interfaces.
