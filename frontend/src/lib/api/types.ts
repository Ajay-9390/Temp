// ─── Request types ────────────────────────────────────────────────────────────

export interface POAttainmentCalculationRequest {
  programId: string;
  academicYear: string;
  calculationMethod?: string;
  calculationVersion?: string;
}

export interface PSOAttainmentCalculationRequest {
  programId: string;
  academicYear: string;
  calculationMethod?: string;
  calculationVersion?: string;
}

export interface CombinedAttainmentCalculationRequest {
  programId: string;
  academicYear: string;
  calculationMethod?: string;
  calculationVersion?: string;
}

// ─── Response types ───────────────────────────────────────────────────────────

export interface POAttainmentResponse {
  id: string;
  programId: string;
  poId: string;
  poCode: string;
  poDescription: string;
  academicYear: string;
  directAttainment: number | null;
  indirectAttainment: number | null;
  finalAttainment: number | null;
  calculationMethod: string;
  calculationVersion: string;
  status: AttainmentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface PSOAttainmentResponse {
  id: string;
  programId: string;
  psoId: string;
  psoCode: string;
  psoDescription: string;
  academicYear: string;
  directAttainment: number | null;
  indirectAttainment: number | null;
  finalAttainment: number | null;
  calculationMethod: string;
  calculationVersion: string;
  status: AttainmentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface CombinedAttainmentResponse {
  poResults: POAttainmentResponse[];
  psoResults: PSOAttainmentResponse[];
}

export interface AttainmentSummaryResponse {
  poAttainments: POAttainmentResponse[];
  psoAttainments: PSOAttainmentResponse[];
  averagePOAttainment: number | null;
  averagePSOAttainment: number | null;
  highestPO: POAttainmentResponse | null;
  lowestPO: POAttainmentResponse | null;
  highestPSO: PSOAttainmentResponse | null;
  lowestPSO: PSOAttainmentResponse | null;
}

// ─── Trace types ──────────────────────────────────────────────────────────────

export interface CODetail {
  coId: string;
  coCode: string;
  attainment: number;
  mappingLevel: number;
  weightedValue: number;
}

export interface CourseBreakdown {
  courseId: string;
  courseName: string;
  cos: CODetail[];
}

export interface POAttainmentTraceResponse {
  id: string;
  programId: string;
  poId: string;
  poCode: string;
  poDescription: string;
  academicYear: string;
  directAttainment: number | null;
  indirectAttainment: number | null;
  finalAttainment: number | null;
  calculationMethod: string;
  calculationVersion: string;
  formula: string;
  totalWeight: number;
  weightedSum: number;
  courseBreakdown: CourseBreakdown[];
  createdAt: string;
}

export interface PSOAttainmentTraceResponse {
  id: string;
  programId: string;
  psoId: string;
  psoCode: string;
  psoDescription: string;
  academicYear: string;
  directAttainment: number | null;
  indirectAttainment: number | null;
  finalAttainment: number | null;
  calculationMethod: string;
  calculationVersion: string;
  formula: string;
  totalWeight: number;
  weightedSum: number;
  courseBreakdown: CourseBreakdown[];
  createdAt: string;
}

// ─── Error type ───────────────────────────────────────────────────────────────

export interface ApiError {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

export type AttainmentStatus = "Excellent" | "Good" | "Needs Improvement";

export const MOCK_PROGRAM_ID = "11111111-0000-0000-0000-000000000001";
export const DEFAULT_ACADEMIC_YEAR = "2023-24";
