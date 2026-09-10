import { useQuery } from "@tanstack/react-query";
import { getPOAttainments, getPOAttainmentById } from "@/lib/api/attainment";

export const poAttainmentKeys = {
  all: ["po-attainment"] as const,
  list: (programId: string, academicYear?: string) =>
    [...poAttainmentKeys.all, "list", programId, academicYear] as const,
  detail: (id: string) =>
    [...poAttainmentKeys.all, "detail", id] as const,
};

export function usePOAttainments(programId: string, academicYear?: string) {
  return useQuery({
    queryKey: poAttainmentKeys.list(programId, academicYear),
    queryFn: () => getPOAttainments(programId, academicYear),
    enabled: !!programId,
    staleTime: 30_000,
  });
}

export function usePOAttainmentById(id: string) {
  return useQuery({
    queryKey: poAttainmentKeys.detail(id),
    queryFn: () => getPOAttainmentById(id),
    enabled: !!id,
    staleTime: 30_000,
  });
}
