import { z } from "zod";

export const departmentSchema = z.object({
  institutionId: z.string().uuid("Select an institution"),
  name: z.string().min(1, "Department name is required").max(200),
  code: z.string().min(1, "Department code is required").max(50),
  description: z.string().max(1000).optional().or(z.literal("")),
  hodUserId: z.string().max(128).optional().or(z.literal("")),
});

export type DepartmentFormValues = z.infer<typeof departmentSchema>;
