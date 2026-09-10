import { z } from "zod";

export const academicYearSchema = z
  .object({
    name: z.string().min(1, "Academic year name is required").max(50),
    startDate: z.string().min(1, "Start date is required"),
    endDate: z.string().min(1, "End date is required"),
  })
  .refine((v) => new Date(v.startDate) < new Date(v.endDate), {
    message: "Start date must be before end date",
    path: ["endDate"],
  });

export type AcademicYearFormValues = z.infer<typeof academicYearSchema>;
