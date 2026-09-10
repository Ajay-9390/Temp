"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api/client";
import type { PageResponse, Program, ProgramOverview, ProgramStatus } from "@/lib/api/types";
import type { ProgramFormValues } from "./schema";

export interface ProgramListParams {
  departmentId?: string;
  status?: ProgramStatus;
  search?: string;
  page?: number;
  size?: number;
  sort?: string;
}

const KEY = "programs";

export function usePrograms(params: ProgramListParams) {
  return useQuery({
    queryKey: [KEY, params],
    queryFn: () =>
      apiRequest<PageResponse<Program>>("/programs", {
        query: {
          departmentId: params.departmentId,
          status: params.status,
          search: params.search,
          page: params.page ?? 0,
          size: params.size ?? 10,
          sort: params.sort,
        },
      }),
  });
}

export function useProgram(id: string | undefined) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: () => apiRequest<Program>(`/programs/${id}`),
    enabled: !!id,
  });
}

export function useProgramOverview(id: string | undefined) {
  return useQuery({
    queryKey: [KEY, "overview", id],
    queryFn: () => apiRequest<ProgramOverview>(`/programs/${id}/overview`),
    enabled: !!id,
  });
}

export function useCreateProgram() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: ProgramFormValues) =>
      apiRequest<Program>("/programs", { method: "POST", body: values }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useUpdateProgram(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: ProgramFormValues) =>
      apiRequest<Program>(`/programs/${id}`, { method: "PUT", body: values }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useProgramStatusUpdate(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (status: ProgramStatus) =>
      apiRequest<Program>(`/programs/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}
