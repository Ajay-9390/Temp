"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api/client";
import type { AcademicLifecycleStatus, AcademicYear } from "@/lib/api/types";
import type { AcademicYearFormValues } from "./schema";

const KEY = "academic-years";

export function useAcademicYears(programId: string | undefined) {
  return useQuery({
    queryKey: [KEY, "byProgram", programId],
    queryFn: () => apiRequest<AcademicYear[]>(`/programs/${programId}/academic-years`),
    enabled: !!programId,
  });
}

export function useAcademicYear(id: string | undefined) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: () => apiRequest<AcademicYear>(`/academic-years/${id}`),
    enabled: !!id,
  });
}

export function useCreateAcademicYear(programId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: AcademicYearFormValues) =>
      apiRequest<AcademicYear>(`/programs/${programId}/academic-years`, { method: "POST", body: values }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useUpdateAcademicYear(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: AcademicYearFormValues) =>
      apiRequest<AcademicYear>(`/academic-years/${id}`, { method: "PUT", body: values }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useAcademicYearStatusUpdate(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (status: AcademicLifecycleStatus) =>
      apiRequest<AcademicYear>(`/academic-years/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}
