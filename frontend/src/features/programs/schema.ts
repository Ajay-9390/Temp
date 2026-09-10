import { z } from "zod";

/** Mirrors backend ProgramRequest validation (frontend is first line; backend is authority). */
export const programSchema = z.object({
  departmentId: z.string().uuid("Select a department"),
  name: z.string().min(1, "Program name is required").max(200),
  code: z.string().min(1, "Program code is required").max(50),
  degree: z.string().max(100).optional().or(z.literal("")),
  branch: z.string().max(150).optional().or(z.literal("")),
  description: z.string().max(2000).optional().or(z.literal("")),
  durationYears: z.coerce.number().int().positive("Duration must be greater than 0"),
  totalSemesters: z.coerce.number().int().positive("Total semesters must be greater than 0"),
  intake: z.coerce.number().int().min(0, "Intake must be zero or greater"),
  establishedYear: z.coerce
    .number()
    .int()
    .min(1800, "Enter a valid year")
    .max(new Date().getFullYear() + 1, "Enter a valid year")
    .optional(),
});

export type ProgramFormValues = z.infer<typeof programSchema>;
