import { z } from "zod";

export const semesterSchema = z
  .object({
    semesterNumber: z.coerce.number().int().positive("Semester number must be greater than 0"),
    name: z.string().min(1, "Semester name is required").max(100),
    startDate: z.string().optional().or(z.literal("")),
    endDate: z.string().optional().or(z.literal("")),
  })
  .refine(
    (v) => !v.startDate || !v.endDate || new Date(v.startDate) < new Date(v.endDate),
    { message: "Start date must be before end date", path: ["endDate"] },
  );

export type SemesterFormValues = z.infer<typeof semesterSchema>;
