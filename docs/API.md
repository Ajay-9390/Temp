# NBA Evidence Management — REST API Contract

Base path: `/api/v1/evidences` • Media: `application/json` (uploads use `multipart/form-data`).
Interactive docs: `GET /swagger-ui.html` (springdoc). OpenAPI JSON: `GET /v3/api-docs`.

All responses use DTOs; JPA entities are never exposed. Errors use a shared
`ApiError` shape (see bottom). Mock identity is taken from `X-User-Id` / `X-User-Name`
headers when present.

## Conventions
- IDs are UUID strings.
- Timestamps are ISO-8601 (`OffsetDateTime`).
- Collections are paginated with `?page=0&size=20&sort=createdAt,desc`.
- Paginated responses use `PageResponse<T> { content, page, size, totalElements, totalPages, first, last }`.

---

## Evidence CRUD

| Method | Path                      | Body / Params                    | Success | Notes |
|--------|---------------------------|----------------------------------|---------|-------|
| POST   | `/api/v1/evidences`       | `CreateEvidenceRequest`          | 201     | Creates evidence in `DRAFT`. Optional multipart first file via `/versions`. |
| GET    | `/api/v1/evidences`       | filters (below) + pagination     | 200     | `PageResponse<EvidenceSummaryResponse>` |
| GET    | `/api/v1/evidences/{id}`  | —                                | 200     | `EvidenceResponse` (full, with counts) |
| PUT    | `/api/v1/evidences/{id}`  | `UpdateEvidenceRequest`          | 200     | Metadata only; not allowed on `ARCHIVED`. |
| DELETE | `/api/v1/evidences/{id}`  | —                                | 204     | Soft-archivable; hard delete only from `DRAFT`. |

**List filters (query params):** `programId`, `departmentId`, `academicYearId`,
`criterionId`, `requirementId`, `category`, `status`, `fileType`, `uploadedBy`,
`keyword` (title/description/filename/tags/extracted text), `createdFrom`, `createdTo`.

### CreateEvidenceRequest
```json
{
  "title": "FDP Certificate - AI/ML",
  "description": "Faculty development program completion certificate",
  "category": "FACULTY",
  "programId": "0b1c...",
  "departmentId": "6a2d...",
  "academicYearId": "9f3e...",
  "criterionId": "c5-uuid",
  "requirementId": "r52-uuid",
  "tags": ["fdp", "training", "2025"]
}
```

### EvidenceResponse (abridged)
```json
{
  "id": "…", "title": "…", "description": "…", "category": "FACULTY",
  "programId": "…", "departmentId": "…", "academicYearId": "…",
  "criterionId": "…", "requirementId": "…",
  "status": "UNDER_REVIEW", "currentVersion": 3, "tags": ["fdp"],
  "uploadedBy": "mock-user", "createdAt": "…", "updatedAt": "…",
  "versionCount": 3, "mappingCount": 3, "reviewCount": 2,
  "latestVersion": { "versionNumber": 3, "fileName": "cert.pdf", "fileType": "pdf",
                     "mimeType": "application/pdf", "fileSize": 152340,
                     "checksum": "…", "ocrStatus": "COMPLETED" }
}
```

---

## Lifecycle actions

| Method | Path                                        | Body                        | Result status |
|--------|---------------------------------------------|-----------------------------|---------------|
| POST   | `/api/v1/evidences/{id}/submit`             | `{ "comment": "…" }` (opt)  | → SUBMITTED   |
| POST   | `/api/v1/evidences/{id}/start-review`       | —                           | → UNDER_REVIEW|
| POST   | `/api/v1/evidences/{id}/approve`            | `ReviewActionRequest`       | → APPROVED    |
| POST   | `/api/v1/evidences/{id}/reject`             | `ReviewActionRequest` (comment required) | → REJECTED |
| POST   | `/api/v1/evidences/{id}/request-changes`    | `ReviewActionRequest` (comment required) | → CHANGES_REQUIRED |
| POST   | `/api/v1/evidences/{id}/archive`            | `{ "reason": "…" }` (opt)   | → ARCHIVED    |

Invalid transitions → `409 Conflict` with `InvalidStateTransitionException`.
Missing required comment → `400 Bad Request`.

```json
// ReviewActionRequest
{ "reviewer": "dr-sharma", "comments": "Certificate is not signed. Please re-upload." }
```

---

## Versions

| Method | Path                                        | Body                        | Success |
|--------|---------------------------------------------|-----------------------------|---------|
| GET    | `/api/v1/evidences/{id}/versions`           | —                           | 200 `List<VersionResponse>` |
| GET    | `/api/v1/evidences/{id}/versions/{n}`       | —                           | 200 `VersionResponse` |
| POST   | `/api/v1/evidences/{id}/versions`           | `multipart: file, changeReason` | 201 `VersionResponse` |
| GET    | `/api/v1/evidences/{id}/versions/{n}/download` | —                        | 302 → signed URL / 200 stream |
| GET    | `/api/v1/evidences/{id}/versions/{n}/preview`  | —                        | 302 → signed URL |

Uploading a version validates the file, stores it once, computes SHA-256, extracts
metadata synchronously, and queues OCR asynchronously.

---

## Mappings

| Method | Path                                             | Body                    | Success |
|--------|--------------------------------------------------|-------------------------|---------|
| GET    | `/api/v1/evidences/{id}/mappings`                | —                       | 200 `List<MappingResponse>` |
| POST   | `/api/v1/evidences/{id}/mappings`                | `CreateMappingRequest`  | 201 `MappingResponse` |
| DELETE | `/api/v1/evidences/{id}/mappings/{mappingId}`    | —                       | 204 |

```json
// CreateMappingRequest
{ "criterionId": "c6-uuid", "requirementId": "r61-uuid", "mappingType": "SUPPORTING" }
```
Duplicate `(criterionId, requirementId)` for same evidence → `409 Conflict`.

---

## Reviews

| Method | Path                                    | Success |
|--------|-----------------------------------------|---------|
| GET    | `/api/v1/evidences/{id}/reviews`        | 200 `List<ReviewResponse>` (history, newest first) |

---

## Audit

| Method | Path                                    | Success |
|--------|-----------------------------------------|---------|
| GET    | `/api/v1/evidences/{id}/audit`          | 200 `List<AuditResponse>` |

---

## Statistics & Gaps

| Method | Path                              | Params                                  | Success |
|--------|-----------------------------------|-----------------------------------------|---------|
| GET    | `/api/v1/evidences/statistics`    | `programId?`, `academicYearId?`         | 200 `StatisticsResponse` |
| GET    | `/api/v1/evidences/gaps`          | `programId`, `academicYearId`, `criterionId?`, `status?` | 200 `GapReportResponse` |

```json
// StatisticsResponse
{
  "totalEvidence": 137, "required": 150,
  "byStatus": { "DRAFT": 12, "SUBMITTED": 9, "UNDER_REVIEW": 8,
                "CHANGES_REQUIRED": 10, "APPROVED": 120, "REJECTED": 5, "ARCHIVED": 3 },
  "byCategory": { "FACULTY": 40, "COURSE_FILE": 55, "…": 0 },
  "approved": 120, "underReview": 8, "changesRequired": 10, "rejected": 5,
  "missing": 13
}

// GapReportResponse
{
  "programId": "…", "academicYearId": "…",
  "totalRequired": 150, "uploaded": 137, "approved": 120,
  "pending": 17, "rejected": 5, "missing": 13,
  "items": [
    { "criterionId": "c5", "requirementId": "r53",
      "expectedCategory": "FACULTY", "expectedEvidence": "FDP records",
      "currentStatus": "MISSING", "evidenceId": null }
  ]
}
```

---

## Reports

| Method | Path                                      | Params                         | Produces |
|--------|-------------------------------------------|--------------------------------|----------|
| GET    | `/api/v1/evidences/reports/completion`    | `programId`, `academicYearId`, `format=xlsx\|pdf\|csv` | file |
| GET    | `/api/v1/evidences/reports/status`        | filters + `format`             | file |

---

## Error shape (`@RestControllerAdvice`)

```json
{
  "timestamp": "2026-09-09T10:15:30Z",
  "status": 409,
  "error": "Conflict",
  "code": "INVALID_STATE_TRANSITION",
  "message": "Cannot transition evidence from APPROVED to UNDER_REVIEW",
  "path": "/api/v1/evidences/123/start-review",
  "fieldErrors": []
}
```

Status codes used: `200, 201, 204, 400 (validation), 404 (not found),
409 (invalid transition / duplicate), 413 (file too large), 415 (unsupported media),
422 (invalid/corrupt file), 500`.
