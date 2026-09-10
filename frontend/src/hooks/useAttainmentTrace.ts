import { useQuery } from "@tanstack/react-query";
import { getPOTrace, getPSOTrace } from "@/lib/api/attainment";

export const traceKeys = {
  po: (id: string) => ["po-trace", id] as const,
  pso: (id: string) => ["pso-trace", id] as const,
};

export function usePOTrace(id: string) {
  return useQuery({
    queryKey: traceKeys.po(id),
    queryFn: () => getPOTrace(id),
    enabled: !!id,
    staleTime: 60_000,
  });
}

export function usePSOTrace(id: string) {
  return useQuery({
    queryKey: traceKeys.pso(id),
    queryFn: () => getPSOTrace(id),
    enabled: !!id,
    staleTime: 60_000,
  });
}
