import { useMutation, useQueryClient } from "@tanstack/react-query";
import {
  calculatePOAttainment,
  calculatePSOAttainment,
  calculateAllAttainment,
} from "@/lib/api/attainment";
import type {
  POAttainmentCalculationRequest,
  PSOAttainmentCalculationRequest,
  CombinedAttainmentCalculationRequest,
} from "@/lib/api/types";
import { poAttainmentKeys } from "./usePOAttainment";
import { psoAttainmentKeys } from "./usePSOAttainment";
import { summaryKeys } from "./useAttainmentSummary";

export function useCalculatePOAttainment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (req: POAttainmentCalculationRequest) =>
      calculatePOAttainment(req),
    onSuccess: (_data, variables) => {
      // Invalidate list and summary queries so the UI refreshes
      queryClient.invalidateQueries({
        queryKey: poAttainmentKeys.list(variables.programId, variables.academicYear),
      });
      queryClient.invalidateQueries({
        queryKey: summaryKeys.summary(variables.programId, variables.academicYear),
      });
    },
  });
}

export function useCalculatePSOAttainment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (req: PSOAttainmentCalculationRequest) =>
      calculatePSOAttainment(req),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: psoAttainmentKeys.list(variables.programId, variables.academicYear),
      });
      queryClient.invalidateQueries({
        queryKey: summaryKeys.summary(variables.programId, variables.academicYear),
      });
    },
  });
}

export function useCalculateAllAttainment() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (req: CombinedAttainmentCalculationRequest) =>
      calculateAllAttainment(req),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({
        queryKey: poAttainmentKeys.list(variables.programId, variables.academicYear),
      });
      queryClient.invalidateQueries({
        queryKey: psoAttainmentKeys.list(variables.programId, variables.academicYear),
      });
      queryClient.invalidateQueries({
        queryKey: summaryKeys.summary(variables.programId, variables.academicYear),
      });
    },
  });
}
