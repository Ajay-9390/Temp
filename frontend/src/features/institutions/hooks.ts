"use client";

import { useQuery } from "@tanstack/react-query";
import { apiRequest } from "@/lib/api/client";
import type { Institution, PageResponse } from "@/lib/api/types";

const KEY = "institutions";

export function useInstitutions() {
  return useQuery({
    queryKey: [KEY],
    queryFn: () => apiRequest<PageResponse<Institution>>("/institutions", { query: { size: 100 } }),
  });
}

export function useInstitution(id: string | undefined) {
  return useQuery({
    queryKey: [KEY, "detail", id],
    queryFn: () => apiRequest<Institution>(`/institutions/${id}`),
    enabled: !!id,
  });
}
