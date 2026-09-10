import { z } from "zod";

export const accreditationCycleSchema = z
  .object({
    name: z.string().min(1, "Cycle name is required").max(150),
    tier: z.string().min(1, "Tier is required"),
    frameworkVersion: z.string().min(1, "Framework version is required").max(100),
    applicationYear: z.coerce.number().int().min(1900, "Enter a valid year"),
    startDate: z.string().optional().or(z.literal("")),
    endDate: z.string().optional().or(z.literal("")),
    remarks: z.string().max(2000).optional().or(z.literal("")),
  })
  .refine(
    (v) => !v.startDate || !v.endDate || new Date(v.startDate) < new Date(v.endDate),
    { message: "Start date must be before end date", path: ["endDate"] },
  );

export type AccreditationCycleFormValues = z.infer<typeof accreditationCycleSchema>;
