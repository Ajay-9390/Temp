"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api/client";
import type { AccreditationCycle, AccreditationCycleStatus } from "@/lib/api/types";
import type { AccreditationCycleFormValues } from "./schema";

const KEY = "accreditation-cycles";

export function useAccreditationCycles(programId: string | undefined) {
  return useQuery({
    queryKey: [KEY, "byProgram", programId],
    queryFn: () => apiRequest<AccreditationCycle[]>(`/programs/${programId}/accreditation-cycles`),
    enabled: !!programId,
  });
}

export function useAccreditationCycle(id: string | undefined) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: () => apiRequest<AccreditationCycle>(`/accreditation-cycles/${id}`),
    enabled: !!id,
  });
}

export function useCreateAccreditationCycle(programId: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: AccreditationCycleFormValues) =>
      apiRequest<AccreditationCycle>(`/programs/${programId}/accreditation-cycles`, {
        method: "POST",
        body: normalize(values),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useUpdateAccreditationCycle(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: AccreditationCycleFormValues) =>
      apiRequest<AccreditationCycle>(`/accreditation-cycles/${id}`, {
        method: "PUT",
        body: normalize(values),
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useCycleStatusUpdate(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (payload: { status: AccreditationCycleStatus; remarks?: string }) =>
      apiRequest<AccreditationCycle>(`/accreditation-cycles/${id}/status`, {
        method: "PATCH",
        body: payload,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

function normalize(values: AccreditationCycleFormValues) {
  return {
    ...values,
    startDate: values.startDate || null,
    endDate: values.endDate || null,
    remarks: values.remarks || null,
  };
}
