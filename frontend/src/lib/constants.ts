import type { EvidenceCategory, EvidenceStatus, MappingType, OcrStatus } from "./types";

export const STATUS_META: Record<EvidenceStatus, { label: string; className: string }> = {
  DRAFT: { label: "Draft", className: "bg-slate-100 text-slate-700 border-slate-200" },
  SUBMITTED: { label: "Submitted", className: "bg-blue-100 text-blue-700 border-blue-200" },
  UNDER_REVIEW: { label: "Under Review", className: "bg-amber-100 text-amber-800 border-amber-200" },
  CHANGES_REQUIRED: { label: "Changes Required", className: "bg-orange-100 text-orange-800 border-orange-200" },
  APPROVED: { label: "Approved", className: "bg-emerald-100 text-emerald-700 border-emerald-200" },
  REJECTED: { label: "Rejected", className: "bg-red-100 text-red-700 border-red-200" },
  ARCHIVED: { label: "Archived", className: "bg-zinc-100 text-zinc-600 border-zinc-200" },
};

export const CATEGORY_LABELS: Record<EvidenceCategory, string> = {
  COURSE_FILE: "Course File",
  ASSESSMENT: "Assessment",
  FACULTY: "Faculty",
  STUDENT: "Student",
  LABORATORY: "Laboratory",
  INFRASTRUCTURE: "Infrastructure",
  PLACEMENT: "Placement",
  FEEDBACK: "Feedback",
  RESEARCH: "Research",
  TRAINING: "Training",
  ACADEMIC: "Academic",
  ADMINISTRATIVE: "Administrative",
  OTHER: "Other",
};

export const MAPPING_TYPE_LABELS: Record<MappingType, string> = {
  PRIMARY: "Primary",
  SUPPORTING: "Supporting",
  SUPPLEMENTARY: "Supplementary",
};

export const OCR_STATUS_META: Record<OcrStatus, { label: string; className: string }> = {
  PENDING: { label: "OCR Pending", className: "bg-amber-100 text-amber-800" },
  PROCESSING: { label: "OCR Processing", className: "bg-blue-100 text-blue-700" },
  COMPLETED: { label: "Text Extracted", className: "bg-emerald-100 text-emerald-700" },
  FAILED: { label: "OCR Failed", className: "bg-red-100 text-red-700" },
  SKIPPED: { label: "No OCR", className: "bg-slate-100 text-slate-600" },
};

export const FILE_TYPE_OPTIONS = [
  "pdf",
  "doc",
  "docx",
  "xls",
  "xlsx",
  "ppt",
  "pptx",
  "jpg",
  "jpeg",
  "png",
  "csv",
] as const;

export const ACCEPTED_FILE_EXTENSIONS = FILE_TYPE_OPTIONS.map((e) => `.${e}`).join(",");
export const MAX_FILE_SIZE_BYTES = 50 * 1024 * 1024; // 50 MB (mirror backend)
