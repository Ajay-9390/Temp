"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api/client";
import type { Department, DepartmentStatus, PageResponse, Program } from "@/lib/api/types";
import type { DepartmentFormValues } from "./schema";

const KEY = "departments";

export interface DepartmentListParams {
  institutionId?: string;
  status?: DepartmentStatus;
  search?: string;
  page?: number;
  size?: number;
}

export function useDepartments(params: DepartmentListParams = {}) {
  return useQuery({
    queryKey: [KEY, params],
    queryFn: () =>
      apiRequest<PageResponse<Department>>("/departments", {
        query: {
          institutionId: params.institutionId,
          status: params.status,
          search: params.search,
          page: params.page ?? 0,
          size: params.size ?? 100,
        },
      }),
  });
}

export function useDepartment(id: string | undefined) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: () => apiRequest<Department>(`/departments/${id}`),
    enabled: !!id,
  });
}

export function useDepartmentPrograms(id: string | undefined) {
  return useQuery({
    queryKey: [KEY, "programs", id],
    queryFn: () => apiRequest<PageResponse<Program>>(`/departments/${id}/programs`, { query: { size: 100 } }),
    enabled: !!id,
  });
}

export function useCreateDepartment() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: DepartmentFormValues) =>
      apiRequest<Department>("/departments", { method: "POST", body: values }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useUpdateDepartment(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (values: DepartmentFormValues) =>
      apiRequest<Department>(`/departments/${id}`, { method: "PUT", body: values }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}

export function useDepartmentStatusUpdate(id: string) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (status: DepartmentStatus) =>
      apiRequest<Department>(`/departments/${id}/status`, { method: "PATCH", body: { status } }),
    onSuccess: () => qc.invalidateQueries({ queryKey: [KEY] }),
  });
}
