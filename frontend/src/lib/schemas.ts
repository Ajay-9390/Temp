import { z } from "zod";
import { EVIDENCE_CATEGORIES, MAPPING_TYPES } from "./types";

const optionalUuid = z
  .union([z.literal(""), z.string().uuid("Must be a valid UUID")])
  .optional();

const categoryEnum = z.enum(
  EVIDENCE_CATEGORIES as unknown as [string, ...string[]],
);
const mappingTypeEnum = z.enum(
  MAPPING_TYPES as unknown as [string, ...string[]],
);

export const evidenceMetadataSchema = z.object({
  title: z.string().trim().min(1, "Title is required").max(255, "Max 255 characters"),
  description: z.string().max(5000, "Max 5000 characters").optional(),
  category: categoryEnum,
  programId: optionalUuid,
  departmentId: optionalUuid,
  academicYearId: optionalUuid,
  criterionId: optionalUuid,
  requirementId: optionalUuid,
  tags: z.string().optional(),
});

export type EvidenceMetadataForm = z.infer<typeof evidenceMetadataSchema>;

export const mappingSchema = z.object({
  criterionId: z.string().uuid("Must be a valid UUID"),
  requirementId: z.string().uuid("Must be a valid UUID"),
  mappingType: mappingTypeEnum.default("SUPPORTING"),
});

export type MappingForm = z.infer<typeof mappingSchema>;

export const reviewSchema = z.object({
  reviewer: z.string().max(120).optional(),
  comments: z.string().max(5000).optional(),
});

export type ReviewForm = z.infer<typeof reviewSchema>;

/** Convert the tags text field to a clean string array. */
export function parseTags(tags?: string): string[] {
  if (!tags) return [];
  return tags
    .split(",")
    .map((t) => t.trim())
    .filter(Boolean);
}

/** Strip empty-string optional fields to undefined for the API payload. */
export function cleanMetadata(form: EvidenceMetadataForm) {
  return {
    title: form.title,
    description: form.description || undefined,
    category: form.category,
    programId: form.programId || undefined,
    departmentId: form.departmentId || undefined,
    academicYearId: form.academicYearId || undefined,
    criterionId: form.criterionId || undefined,
    requirementId: form.requirementId || undefined,
    tags: parseTags(form.tags),
  };
}
