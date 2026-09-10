import { useQuery } from "@tanstack/react-query";
import { getAttainmentSummary } from "@/lib/api/attainment";

export const summaryKeys = {
  all: ["attainment-summary"] as const,
  summary: (programId: string, academicYear?: string) =>
    [...summaryKeys.all, programId, academicYear] as const,
};

export function useAttainmentSummary(programId: string, academicYear?: string) {
  return useQuery({
    queryKey: summaryKeys.summary(programId, academicYear),
    queryFn: () => getAttainmentSummary(programId, academicYear),
    enabled: !!programId,
    staleTime: 30_000,
  });
}
