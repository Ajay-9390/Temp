// Types mirroring the backend DTOs (com.accreditation.nba.evidence.dto).

export const EVIDENCE_CATEGORIES = [
  "COURSE_FILE",
  "ASSESSMENT",
  "FACULTY",
  "STUDENT",
  "LABORATORY",
  "INFRASTRUCTURE",
  "PLACEMENT",
  "FEEDBACK",
  "RESEARCH",
  "TRAINING",
  "ACADEMIC",
  "ADMINISTRATIVE",
  "OTHER",
] as const;
export type EvidenceCategory = (typeof EVIDENCE_CATEGORIES)[number];

export const EVIDENCE_STATUSES = [
  "DRAFT",
  "SUBMITTED",
  "UNDER_REVIEW",
  "CHANGES_REQUIRED",
  "APPROVED",
  "REJECTED",
  "ARCHIVED",
] as const;
export type EvidenceStatus = (typeof EVIDENCE_STATUSES)[number];

export const MAPPING_TYPES = ["PRIMARY", "SUPPORTING", "SUPPLEMENTARY"] as const;
export type MappingType = (typeof MAPPING_TYPES)[number];

export type ReviewDecision = "APPROVE" | "REJECT" | "REQUEST_CHANGES";
export type OcrStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "FAILED" | "SKIPPED";
export type AuditAction = string;

export interface VersionResponse {
  id: string;
  evidenceId: string;
  versionNumber: number;
  fileName: string;
  fileType: string | null;
  mimeType: string | null;
  fileSize: number;
  checksum: string | null;
  uploadedBy: string;
  changeReason: string | null;
  pageCount: number | null;
  detectedLanguage: string | null;
  ocrStatus: OcrStatus | null;
  ocrProcessedAt: string | null;
  hasExtractedText: boolean;
  textPreview: string | null;
  createdAt: string;
}

export interface EvidenceResponse {
  id: string;
  title: string;
  description: string | null;
  category: EvidenceCategory;
  programId: string | null;
  departmentId: string | null;
  academicYearId: string | null;
  criterionId: string | null;
  requirementId: string | null;
  status: EvidenceStatus;
  currentVersion: number;
  tags: string[];
  uploadedBy: string;
  createdAt: string;
  updatedAt: string;
  versionCount: number;
  mappingCount: number;
  reviewCount: number;
  latestVersion: VersionResponse | null;
}

export interface EvidenceSummaryResponse {
  id: string;
  title: string;
  category: EvidenceCategory;
  programId: string | null;
  academicYearId: string | null;
  criterionId: string | null;
  requirementId: string | null;
  status: EvidenceStatus;
  currentVersion: number;
  latestFileType: string | null;
  uploadedBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface MappingResponse {
  id: string;
  evidenceId: string;
  criterionId: string;
  requirementId: string;
  mappingType: MappingType;
  createdBy: string | null;
  createdAt: string;
}

export interface ReviewResponse {
  id: string;
  evidenceId: string;
  versionNumber: number | null;
  reviewer: string;
  decision: ReviewDecision;
  resultingStatus: EvidenceStatus;
  comments: string | null;
  reviewedAt: string;
}

export interface AuditResponse {
  id: string;
  evidenceId: string;
  action: AuditAction;
  entityType: string;
  entityId: string | null;
  performedBy: string;
  oldValue: string | null;
  newValue: string | null;
  reason: string | null;
  createdAt: string;
}

export interface StatisticsResponse {
  programId: string | null;
  academicYearId: string | null;
  totalEvidence: number;
  required: number;
  draft: number;
  submitted: number;
  underReview: number;
  changesRequired: number;
  approved: number;
  rejected: number;
  archived: number;
  missing: number;
  byStatus: Record<EvidenceStatus, number>;
  byCategory: Record<EvidenceCategory, number>;
}

export interface GapItemResponse {
  criterionId: string;
  requirementId: string;
  criterionCode: string;
  requirementCode: string;
  expectedCategory: EvidenceCategory;
  expectedEvidence: string;
  mandatory: boolean;
  currentStatus: string;
  evidenceId: string | null;
  satisfied: boolean;
}

export interface GapReportResponse {
  programId: string | null;
  academicYearId: string | null;
  totalRequired: number;
  uploaded: number;
  approved: number;
  pending: number;
  rejected: number;
  missing: number;
  items: GapItemResponse[];
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  code: string;
  message: string;
  path: string;
  fieldErrors: { field: string; message: string }[];
}
