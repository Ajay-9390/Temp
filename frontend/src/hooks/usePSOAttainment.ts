import { useQuery } from "@tanstack/react-query";
import { getPSOAttainments, getPSOAttainmentById } from "@/lib/api/attainment";

export const psoAttainmentKeys = {
  all: ["pso-attainment"] as const,
  list: (programId: string, academicYear?: string) =>
    [...psoAttainmentKeys.all, "list", programId, academicYear] as const,
  detail: (id: string) =>
    [...psoAttainmentKeys.all, "detail", id] as const,
};

export function usePSOAttainments(programId: string, academicYear?: string) {
  return useQuery({
    queryKey: psoAttainmentKeys.list(programId, academicYear),
    queryFn: () => getPSOAttainments(programId, academicYear),
    enabled: !!programId,
    staleTime: 30_000,
  });
}

export function usePSOAttainmentById(id: string) {
  return useQuery({
    queryKey: psoAttainmentKeys.detail(id),
    queryFn: () => getPSOAttainmentById(id),
    enabled: !!id,
    staleTime: 30_000,
  });
}
