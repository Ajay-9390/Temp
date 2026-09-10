"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api/client";
import type { AcademicLifecycleStatus, Semester } from "@/lib/api/types";
import type { SemesterFormValues } from "./schema";

const KEY = "semesters";

export function useSemesters(academicYearId: string | undefined) {
  return useQuery({
    queryKey: [KEY, "byYear", academicYearId],
    queryFn: () => apiRequest<Semester[]>(`/academic-years/${academicYearId}/semesters`),
    enabled: !!academicYearId,
  });
}

export function useCreateSemester(academicYearId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: SemesterFormValues) =>
      apiRequest<Semester>(`/academic-years/${academicYearId}/semesters`, {
        method: "POST",
        body: normalize(values),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useUpdateSemester(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: SemesterFormValues) =>
      apiRequest<Semester>(`/semesters/${id}`, { method: "PUT", body: normalize(values) }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useSemesterStatusUpdate(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (status: AcademicLifecycleStatus) =>
      apiRequest<Semester>(`/semesters/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

function normalize(values: SemesterFormValues) {
  return { ...values, startDate: values.startDate || null, endDate: values.endDate || null };
}
