# NBA Program Management Module

Production-ready module of the larger NBA (National Board of Accreditation) accreditation
platform. It manages the academic + accreditation context:

```
Institution → Department → Program → AccreditationCycle
                                   → AcademicYear → Semester
```

Built as a **modular monolith** slice: it runs independently today and integrates into the
full platform later without restructuring. It intentionally excludes Outcomes, Attainment,
CQI, Evidence, SAR, AI/RAG, Kafka and Camunda — but exposes clean contracts and stable UUIDs
so those modules can integrate (see `docs/INTEGRATION.md`).

- **Backend**: Java 21, Spring Boot 3, Spring Data JPA, Hibernate + Envers, Flyway,
  PostgreSQL, Redis cache, Spring Security (Keycloak/OIDC resource server), springdoc OpenAPI,
  Actuator/Prometheus.
- **Frontend**: Next.js 14 (App Router), TypeScript, Tailwind, shadcn/ui, TanStack Query,
  React Hook Form + Zod.
- **Infra**: Docker Compose (postgres/redis/keycloak/backend/frontend/nginx/prometheus/grafana).

---

## Quick start (Docker Compose)

```bash
cp .env.example .env        # adjust as needed
docker compose up --build
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Nginx (unified) | http://localhost/ |
| Keycloak | http://localhost:8081 |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 |

Seed data (fictional) loads on first boot (`SEED_ENABLED=true`), and security is disabled by
default for a friction-free demo (`APP_SECURITY_ENABLED=false`).

---

## Run locally without Docker

### Backend
Requires JDK 21+ and a PostgreSQL. Start Postgres (any way), then:
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# dev profile: in-memory cache, seed data on, security off
```
Configure DB via env: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (defaults target
`jdbc:postgresql://localhost:5432/nba_program`, user/pass `nba`/`nba`).

### Frontend
Requires Node 20+.
```bash
cd frontend
npm install
npm run dev        # http://localhost:3000  (expects backend on :8080)
```

---

## Tests

**Backend**
```bash
cd backend
mvn test            # unit tests (JUnit 5 + Mockito) — no Docker needed
mvn verify          # + Testcontainers integration/E2E (requires a Docker daemon)
```
- Unit: lifecycle state machines, service business rules, duplicate prevention, date validation.
- Integration (`ProgramManagementE2EIT`, Testcontainers PostgreSQL): the full §36 scenario —
  Institution → Department → Program → Tier I NBA 2026 cycle → Academic Year 2026-27 →
  Semester 1 & 2 → verify program overview, plus duplicate/invalid-tier rejection. It
  auto-skips when no Docker daemon is available (`disabledWithoutDocker`).

**Frontend**
```bash
cd frontend
npm run build       # type-check + production build
npm run test:e2e    # Playwright (needs backend + frontend running)
```

---

## Definition of Done — status

Backend: CRUD APIs ✓ · validation ✓ · PostgreSQL ✓ · Flyway V1–V7 ✓ · Keycloak-ready
security ✓ · permission checks ✓ · lifecycle transitions ✓ · audit (Envers + custom) ✓ ·
OpenAPI ✓ · unit tests ✓ · Testcontainers E2E ✓.
Frontend: program list/create/edit/details ✓ · accreditation cycles ✓ · academic years ✓ ·
semesters ✓ · validated forms ✓ · TanStack Query ✓ · error handling ✓ · permission-aware UI ✓ ·
Playwright specs ✓.
Integration: stable UUIDs ✓ · documented contracts/DTOs/permissions ✓ · seed data ✓ ·
Docker ✓ · `.env.example` ✓.

---

## Repository layout

```
.
├─ backend/                 Spring Boot modular monolith
│  ├─ src/main/java/com/example/nba/
│  │  ├─ config/ security/ common/{response,exception,audit,logging}
│  │  ├─ institution/ department/ program/ accreditation/
│  │  └─ academic/{year,semester}     (each: controller|service|repository|entity|dto|mapper)
│  ├─ src/main/resources/{application.yml, db/migration/V1..V7}
│  ├─ src/test/java/...                (unit + Testcontainers E2E)
│  └─ Dockerfile
├─ frontend/                Next.js app (App Router, feature-based)
│  ├─ src/app/  src/components/{ui}  src/features/*  src/lib/{api,auth}
│  ├─ e2e/                  Playwright specs
│  └─ Dockerfile
├─ infra/{nginx,prometheus,grafana}
├─ docs/{ARCHITECTURE.md, INTEGRATION.md, PERMISSIONS.md}
├─ docker-compose.yml
└─ .env.example
```

## Documentation

- `docs/ARCHITECTURE.md` — architecture, ER diagram, lifecycle, API surface.
- `docs/INTEGRATION.md` — contracts for the other 11 teams (IDs, DTOs, endpoints, examples).
- `docs/PERMISSIONS.md` — permission catalog + RBAC integration approach.

## Environment variables

See `.env.example` for the full list (DB, Redis, backend toggles, Keycloak, frontend API URL,
nginx/monitoring ports). Secrets are never hardcoded; defaults are for local dev only.

## Seed data

`ABC Institute of Technology` → departments CSE/ECE/Mechanical → B.Tech programs → CSE
accreditation history (2016 EXPIRED, 2021 ACCREDITED, 2026 PREPARATION Tier I) → academic years
2024-25/2025-26/2026-27 → Semester 1 (ACTIVE) & 2 (PLANNED) for 2026-27.
Enable with `SEED_ENABLED=true` (default in dev/compose). Idempotent.

## Notes on scope

No Kafka, Camunda, AI/RAG, Supabase/Tika/PDFBox/Tesseract, or unrelated NBA modules are
implemented — the architecture leaves seams (`@Async` config, lifecycle services,
`DataScopeService` SPI, pgvector-capable Postgres image) so they can be added later without
restructuring.
