"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { api, ApiClientError, type EvidenceListParams } from "@/lib/api";

export const queryKeys = {
  list: (params: EvidenceListParams) => ["evidence", "list", params] as const,
  detail: (id: string) => ["evidence", "detail", id] as const,
  versions: (id: string) => ["evidence", id, "versions"] as const,
  mappings: (id: string) => ["evidence", id, "mappings"] as const,
  reviews: (id: string) => ["evidence", id, "reviews"] as const,
  audit: (id: string) => ["evidence", id, "audit"] as const,
  statistics: (p: object) => ["evidence", "statistics", p] as const,
  gaps: (p: object) => ["evidence", "gaps", p] as const,
};

function errorMessage(error: unknown): string {
  if (error instanceof ApiClientError) {
    if (error.fieldErrors.length) {
      return error.fieldErrors.map((f) => `${f.field}: ${f.message}`).join(", ");
    }
    return error.message;
  }
  return error instanceof Error ? error.message : "Unexpected error";
}

// ---- Queries ----

export function useEvidenceList(params: EvidenceListParams) {
  return useQuery({
    queryKey: queryKeys.list(params),
    queryFn: () => api.evidence.list(params),
    placeholderData: (prev) => prev,
  });
}

export function useEvidence(id: string) {
  return useQuery({
    queryKey: queryKeys.detail(id),
    queryFn: () => api.evidence.get(id),
    enabled: Boolean(id),
  });
}

export function useVersions(id: string) {
  return useQuery({ queryKey: queryKeys.versions(id), queryFn: () => api.versions.list(id), enabled: Boolean(id) });
}

export function useMappings(id: string) {
  return useQuery({ queryKey: queryKeys.mappings(id), queryFn: () => api.mappings.list(id), enabled: Boolean(id) });
}

export function useReviews(id: string) {
  return useQuery({ queryKey: queryKeys.reviews(id), queryFn: () => api.reviews.list(id), enabled: Boolean(id) });
}

export function useAudit(id: string) {
  return useQuery({ queryKey: queryKeys.audit(id), queryFn: () => api.evidence.audit(id), enabled: Boolean(id) });
}

export function useStatistics(params: { programId?: string; academicYearId?: string }) {
  return useQuery({ queryKey: queryKeys.statistics(params), queryFn: () => api.evidence.statistics(params) });
}

export function useGaps(params: { programId?: string; academicYearId?: string; criterionId?: string; status?: string }) {
  return useQuery({ queryKey: queryKeys.gaps(params), queryFn: () => api.evidence.gaps(params) });
}

// ---- Mutations ----

function useInvalidateEvidence(id?: string) {
  const qc = useQueryClient();
  return () => {
    qc.invalidateQueries({ queryKey: ["evidence"] });
    if (id) qc.invalidateQueries({ queryKey: queryKeys.detail(id) });
  };
}

export function useCreateEvidence() {
  const invalidate = useInvalidateEvidence();
  return useMutation({
    mutationFn: (formData: FormData) => api.evidence.createWithFile(formData),
    onSuccess: () => {
      toast.success("Evidence created");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
}

export function useUpdateEvidence(id: string) {
  const invalidate = useInvalidateEvidence(id);
  return useMutation({
    mutationFn: (body: unknown) => api.evidence.update(id, body),
    onSuccess: () => {
      toast.success("Evidence updated");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
}

export function useDeleteEvidence() {
  const invalidate = useInvalidateEvidence();
  return useMutation({
    mutationFn: (id: string) => api.evidence.remove(id),
    onSuccess: () => {
      toast.success("Evidence deleted");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
}

export function useLifecycleAction(id: string) {
  const invalidate = useInvalidateEvidence(id);
  const submit = useMutation({
    mutationFn: (comment?: string) => api.evidence.submit(id, comment),
    onSuccess: () => {
      toast.success("Submitted for review");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
  const startReview = useMutation({
    mutationFn: () => api.evidence.startReview(id),
    onSuccess: () => {
      toast.success("Review started");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
  const archive = useMutation({
    mutationFn: (reason?: string) => api.evidence.archive(id, reason),
    onSuccess: () => {
      toast.success("Evidence archived");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
  return { submit, startReview, archive };
}

export function useUploadVersion(id: string) {
  const invalidate = useInvalidateEvidence(id);
  return useMutation({
    mutationFn: (formData: FormData) => api.versions.upload(id, formData),
    onSuccess: () => {
      toast.success("New version uploaded");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
}

export function useMappingMutations(id: string) {
  const invalidate = useInvalidateEvidence(id);
  const add = useMutation({
    mutationFn: (body: unknown) => api.mappings.add(id, body),
    onSuccess: () => {
      toast.success("Mapping added");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
  const remove = useMutation({
    mutationFn: (mappingId: string) => api.mappings.remove(id, mappingId),
    onSuccess: () => {
      toast.success("Mapping removed");
      invalidate();
    },
    onError: (e) => toast.error(errorMessage(e)),
  });
  return { add, remove };
}

type ReviewBody = { reviewer?: string; comments?: string };

export function useReviewActions(id: string) {
  const invalidate = useInvalidateEvidence(id);
  const handlers = (msg: string) => ({
    onSuccess: () => {
      toast.success(msg);
      invalidate();
    },
    onError: (e: unknown) => toast.error(errorMessage(e)),
  });

  const approve = useMutation({
    mutationFn: (body: ReviewBody) => api.reviews.approve(id, body),
    ...handlers("Evidence approved"),
  });
  const reject = useMutation({
    mutationFn: (body: ReviewBody) => api.reviews.reject(id, body),
    ...handlers("Evidence rejected"),
  });
  const requestChanges = useMutation({
    mutationFn: (body: ReviewBody) => api.reviews.requestChanges(id, body),
    ...handlers("Changes requested"),
  });
  return { approve, reject, requestChanges };
}
