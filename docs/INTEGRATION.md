# NBA Evidence Management — Integration Contracts

This module is developed independently by 1 of 12 developers. It **references other
modules by ID only** and never re-creates their tables. This document defines the
contracts other teams must satisfy and the events this module exposes.

---

## 1. Inbound references (IDs owned by other modules)

The evidence tables store these as **plain, FK-ready UUID columns** with **no database
foreign key** to the other module (to keep migrations independent):

| Field              | Owning module                     | Meaning |
|--------------------|-----------------------------------|---------|
| `programId`        | NBA Program Management            | The program the evidence belongs to |
| `departmentId`     | Program / Org module              | Department reference |
| `academicYearId`   | Calendar Management               | Academic year reference |
| `criterionId`      | NBA Criteria & SAR Management     | NBA criterion the evidence supports |
| `requirementId`    | NBA Criteria & SAR Management     | Specific requirement within a criterion |

When those modules exist, replace the mock providers below with real REST/Feign clients.
No schema change is required — only the provider implementations change.

### 1.1 `EvidenceRequirementProvider` (drives Gap Detection)
Gap detection needs the list of **required** evidence for a program + academic year.
That list is owned by **NBA Criteria & SAR Management**, not this module.

```java
public interface EvidenceRequirementProvider {
    List<RequiredEvidence> getRequiredEvidence(UUID programId, UUID academicYearId);
}

public record RequiredEvidence(
    UUID criterionId, UUID requirementId,
    String criterionCode, String requirementCode,
    EvidenceCategory expectedCategory, String expectedEvidenceName, boolean mandatory) {}
```
Default impl: `MockEvidenceRequirementProvider` (returns a representative sample so the
gap page/report work end-to-end during this phase). Swap for `NbaCriteriaRequirementClient`
later. **This module never creates or edits criteria/requirements.**

### 1.2 Directory clients (optional, for display names)
```java
public interface ProgramDirectoryClient { Optional<ProgramInfo> findProgram(UUID id); }
public interface AcademicYearDirectoryClient { Optional<AcademicYearInfo> findAcademicYear(UUID id); }
public interface NbaCriteriaClient {
    Optional<CriterionInfo> findCriterion(UUID id);
    Optional<RequirementInfo> findRequirement(UUID id);
}
```
Core CRUD/lifecycle works **without** these; they only enrich labels in responses/reports.
Mock implementations return `Optional.empty()` / echo the ID.

### 1.3 Identity (`CurrentUserProvider`)
No auth in this phase. `CurrentUserProvider` returns a mock user:
- reads `X-User-Id` / `X-User-Name` request headers if present,
- otherwise returns `mock-user`.
This is the **only** seam to replace with real auth (JWT/Keycloak) later.

---

## 2. Outbound domain events (Notification / Kafka-ready)

The module publishes Spring `ApplicationEvent`s through a `DomainEventPublisher`
abstraction. Today they are in-process (consumed by the audit + a logging notification
stub). Later, a single adapter can forward them to Kafka — **no business code changes**.

| Event                        | Emitted when                     | Suggested Kafka topic        |
|------------------------------|----------------------------------|------------------------------|
| `EvidenceCreatedEvent`       | evidence created                 | `nba.evidence.created`       |
| `EvidenceSubmittedEvent`     | submitted for review             | `nba.evidence.submitted`     |
| `EvidenceReviewedEvent`      | any review recorded              | `nba.evidence.reviewed`      |
| `EvidenceApprovedEvent`      | approved                         | `nba.evidence.approved`      |
| `EvidenceRejectedEvent`      | rejected                         | `nba.evidence.rejected`      |
| `EvidenceChangesRequestedEvent` | changes requested             | `nba.evidence.changes`       |
| `EvidenceVersionCreatedEvent`| new version uploaded             | `nba.evidence.version`       |
| `EvidenceMappedEvent`        | mapping added                    | `nba.evidence.mapped`        |
| `EvidenceUnmappedEvent`      | mapping removed                  | `nba.evidence.unmapped`      |

Common envelope:
```json
{
  "eventType": "EvidenceApprovedEvent",
  "evidenceId": "…", "occurredAt": "…", "actor": "mock-user",
  "payload": { "status": "APPROVED", "versionNumber": 3 }
}
```

The Notification module can subscribe to `EvidenceSubmitted/Approved/Rejected/ChangesRequested`
without this module knowing about it.

---

## 3. Outbound to SAR modules

Approved evidence is the input to **SAR Management / SAR Generator**. They can query:
- `GET /api/v1/evidences?status=APPROVED&criterionId=…&programId=…&academicYearId=…`
- `GET /api/v1/evidences/{id}/versions/{n}/download` (signed URL)

This module remains the **central repository and lifecycle manager** for NBA supporting
documents; SAR modules consume approved artifacts read-only.

---

## 4. Database boundary rules

- Only `nba_evidence`, `nba_evidence_version`, `nba_evidence_mapping`,
  `nba_evidence_review`, `nba_evidence_audit` (+ Envers `_aud` tables) are created here.
- Migrations live in `backend/src/main/resources/db/migration` with the prefix `V1__`,
  `V2__`, … scoped to this module. **Never edit another developer's migration.**
- No FK constraints cross module boundaries; cross-module integrity is by ID + interface.
- Shared PostgreSQL instance/database — no separate DB or instance is created.
