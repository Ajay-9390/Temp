// Shared API contract types mirroring the backend DTOs (see docs/INTEGRATION.md).

export interface ApiResponse<T> {
  success: boolean;
  data: T;
  message?: string;
  timestamp: string;
}

export interface FieldViolation {
  field: string;
  message: string;
}

export interface ApiErrorBody {
  code: string;
  message: string;
  details?: FieldViolation[];
}

export interface ApiError {
  success: false;
  error: ApiErrorBody;
  timestamp: string;
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

// ---- Status enums ----
export type InstitutionStatus = "ACTIVE" | "INACTIVE";
export type DepartmentStatus = "ACTIVE" | "INACTIVE";
export type ProgramStatus = "DRAFT" | "ACTIVE" | "INACTIVE" | "ARCHIVED";
export type AccreditationCycleStatus =
  | "DRAFT"
  | "PREPARATION"
  | "SUBMITTED"
  | "UNDER_REVIEW"
  | "ACCREDITED"
  | "EXPIRED"
  | "REJECTED"
  | "CANCELLED";
export type AcademicLifecycleStatus = "PLANNED" | "ACTIVE" | "COMPLETED" | "ARCHIVED";

// ---- Entities ----
export interface Institution {
  id: string;
  name: string;
  code: string;
  type?: string;
  address?: string;
  city?: string;
  state?: string;
  country?: string;
  website?: string;
  contactEmail?: string;
  contactPhone?: string;
  status: InstitutionStatus;
  createdAt: string;
  updatedAt: string;
}

export interface Department {
  id: string;
  institutionId: string;
  name: string;
  code: string;
  description?: string;
  hodUserId?: string;
  status: DepartmentStatus;
  programCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface CurrentAccreditation {
  id: string;
  name: string;
  tier: string;
  status: string;
}

export interface CurrentAcademicYear {
  id: string;
  name: string;
  status: string;
}

export interface Program {
  id: string;
  departmentId: string;
  departmentName?: string;
  name: string;
  code: string;
  degree?: string;
  branch?: string;
  description?: string;
  durationYears: number;
  totalSemesters: number;
  intake: number;
  establishedYear?: number;
  status: ProgramStatus;
  currentAccreditation?: CurrentAccreditation;
  currentAcademicYear?: CurrentAcademicYear;
  createdAt: string;
  updatedAt: string;
}

export interface ProgramOverview {
  programId: string;
  programName: string;
  programCode: string;
  programStatus: ProgramStatus;
  departmentId: string;
  departmentName: string;
  institutionId: string;
  institutionName: string;
  currentAccreditation?: CurrentAccreditation;
  tier?: string;
  currentAcademicYear?: CurrentAcademicYear;
  totalSemesters: number;
  accreditationCycleCount: number;
  academicYearCount: number;
  previousAccreditations: CurrentAccreditation[];
  integrationPoints: Record<string, string>;
}

export interface AccreditationCycle {
  id: string;
  programId: string;
  name: string;
  tier: string;
  frameworkVersion: string;
  applicationYear: number;
  startDate?: string;
  endDate?: string;
  status: AccreditationCycleStatus;
  remarks?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AcademicYear {
  id: string;
  programId: string;
  name: string;
  startDate: string;
  endDate: string;
  status: AcademicLifecycleStatus;
  semesterCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface Semester {
  id: string;
  academicYearId: string;
  semesterNumber: number;
  name: string;
  startDate?: string;
  endDate?: string;
  status: AcademicLifecycleStatus;
  createdAt: string;
  updatedAt: string;
}
