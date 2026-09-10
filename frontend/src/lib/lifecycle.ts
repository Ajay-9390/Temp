import type {
  AcademicLifecycleStatus, AccreditationCycleStatus, ProgramStatus,
} from "@/lib/api/types";

// Frontend mirror of the backend state machines. Used only to show the right action buttons;
// the backend remains the authority and rejects invalid transitions with 409.

export const PROGRAM_TRANSITIONS: Record<ProgramStatus, ProgramStatus[]> = {
  DRAFT: ["ACTIVE"],
  ACTIVE: ["INACTIVE"],
  INACTIVE: ["ACTIVE", "ARCHIVED"],
  ARCHIVED: ["INACTIVE"], // restore / unarchive
};

export const ACADEMIC_TRANSITIONS: Record<AcademicLifecycleStatus, AcademicLifecycleStatus[]> = {
  PLANNED: ["ACTIVE"],
  ACTIVE: ["COMPLETED"],
  COMPLETED: ["ARCHIVED"],
  ARCHIVED: ["COMPLETED"], // restore / unarchive
};

export const CYCLE_TRANSITIONS: Record<AccreditationCycleStatus, AccreditationCycleStatus[]> = {
  DRAFT: ["PREPARATION", "CANCELLED"],
  PREPARATION: ["SUBMITTED", "CANCELLED"],
  SUBMITTED: ["UNDER_REVIEW", "CANCELLED"],
  UNDER_REVIEW: ["ACCREDITED", "REJECTED"],
  ACCREDITED: ["EXPIRED"],
  REJECTED: ["PREPARATION"],
  EXPIRED: [],
  CANCELLED: [],
};
