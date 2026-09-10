import type {
  ApiError,
  AuditResponse,
  EvidenceResponse,
  EvidenceSummaryResponse,
  GapReportResponse,
  MappingResponse,
  PageResponse,
  ReviewResponse,
  StatisticsResponse,
  VersionResponse,
} from "./types";

const EVIDENCE_BASE = "/api/v1/evidences";

/** Error carrying the backend ApiError details for UI display. */
export class ApiClientError extends Error {
  status: number;
  code: string;
  fieldErrors: { field: string; message: string }[];

  constructor(error: ApiError) {
    super(error.message || error.error || "Request failed");
    this.name = "ApiClientError";
    this.status = error.status;
    this.code = error.code;
    this.fieldErrors = error.fieldErrors ?? [];
  }
}

type QueryValue = string | number | boolean | null | undefined;

export function buildQuery(params: Record<string, QueryValue>): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      search.append(key, String(value));
    }
  });
  const qs = search.toString();
  return qs ? `?${qs}` : "";
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const headers = new Headers(init?.headers);
  headers.set("Accept", "application/json");
  // Mock identity for the no-auth development phase (backend reads these).
  if (!headers.has("X-User-Id")) headers.set("X-User-Id", "ui-user");
  if (!headers.has("X-User-Name")) headers.set("X-User-Name", "Evidence Officer");

  const response = await fetch(path, { ...init, headers });

  if (response.status === 204) {
    return undefined as T;
  }
  if (!response.ok) {
    let payload: ApiError | null = null;
    try {
      payload = (await response.json()) as ApiError;
    } catch {
      // fall through to generic error
    }
    throw new ApiClientError(
      payload ?? {
        timestamp: new Date().toISOString(),
        status: response.status,
        error: response.statusText,
        code: "HTTP_ERROR",
        message: `Request failed with ${response.status}`,
        path,
        fieldErrors: [],
      },
    );
  }
  return (await response.json()) as T;
}

function json(method: string, body: unknown): RequestInit {
  return {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body ?? {}),
  };
}

export interface EvidenceListParams {
  programId?: string;
  academicYearId?: string;
  criterionId?: string;
  requirementId?: string;
  category?: string;
  status?: string;
  fileType?: string;
  keyword?: string;
  createdFrom?: string;
  createdTo?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const api = {
  evidence: {
    list(params: EvidenceListParams) {
      return request<PageResponse<EvidenceSummaryResponse>>(
        `${EVIDENCE_BASE}${buildQuery(params as Record<string, QueryValue>)}`,
      );
    },
    get(id: string) {
      return request<EvidenceResponse>(`${EVIDENCE_BASE}/${id}`);
    },
    createJson(body: unknown) {
      return request<EvidenceResponse>(EVIDENCE_BASE, json("POST", body));
    },
    createWithFile(formData: FormData) {
      return request<EvidenceResponse>(EVIDENCE_BASE, { method: "POST", body: formData });
    },
    update(id: string, body: unknown) {
      return request<EvidenceResponse>(`${EVIDENCE_BASE}/${id}`, json("PUT", body));
    },
    remove(id: string) {
      return request<void>(`${EVIDENCE_BASE}/${id}`, { method: "DELETE" });
    },
    submit(id: string, comment?: string) {
      return request<EvidenceResponse>(`${EVIDENCE_BASE}/${id}/submit`, json("POST", { comment }));
    },
    startReview(id: string) {
      return request<EvidenceResponse>(`${EVIDENCE_BASE}/${id}/start-review`, json("POST", {}));
    },
    archive(id: string, reason?: string) {
      return request<EvidenceResponse>(`${EVIDENCE_BASE}/${id}/archive`, json("POST", { comment: reason }));
    },
    statistics(params: { programId?: string; academicYearId?: string }) {
      return request<StatisticsResponse>(
        `${EVIDENCE_BASE}/statistics${buildQuery(params as Record<string, QueryValue>)}`,
      );
    },
    gaps(params: { programId?: string; academicYearId?: string; criterionId?: string; status?: string }) {
      return request<GapReportResponse>(
        `${EVIDENCE_BASE}/gaps${buildQuery(params as Record<string, QueryValue>)}`,
      );
    },
    audit(id: string) {
      return request<AuditResponse[]>(`${EVIDENCE_BASE}/${id}/audit`);
    },
  },

  versions: {
    list(id: string) {
      return request<VersionResponse[]>(`${EVIDENCE_BASE}/${id}/versions`);
    },
    upload(id: string, formData: FormData) {
      return request<VersionResponse>(`${EVIDENCE_BASE}/${id}/versions`, {
        method: "POST",
        body: formData,
      });
    },
    downloadUrl(id: string, versionNumber: number) {
      return `${EVIDENCE_BASE}/${id}/versions/${versionNumber}/download`;
    },
    previewUrl(id: string, versionNumber: number) {
      return `${EVIDENCE_BASE}/${id}/versions/${versionNumber}/preview`;
    },
  },

  mappings: {
    list(id: string) {
      return request<MappingResponse[]>(`${EVIDENCE_BASE}/${id}/mappings`);
    },
    add(id: string, body: unknown) {
      return request<MappingResponse>(`${EVIDENCE_BASE}/${id}/mappings`, json("POST", body));
    },
    remove(id: string, mappingId: string) {
      return request<void>(`${EVIDENCE_BASE}/${id}/mappings/${mappingId}`, { method: "DELETE" });
    },
  },

  reviews: {
    list(id: string) {
      return request<ReviewResponse[]>(`${EVIDENCE_BASE}/${id}/reviews`);
    },
    approve(id: string, body: { reviewer?: string; comments?: string }) {
      return request<ReviewResponse>(`${EVIDENCE_BASE}/${id}/approve`, json("POST", body));
    },
    reject(id: string, body: { reviewer?: string; comments?: string }) {
      return request<ReviewResponse>(`${EVIDENCE_BASE}/${id}/reject`, json("POST", body));
    },
    requestChanges(id: string, body: { reviewer?: string; comments?: string }) {
      return request<ReviewResponse>(`${EVIDENCE_BASE}/${id}/request-changes`, json("POST", body));
    },
  },

  reports: {
    completionUrl(params: { programId?: string; academicYearId?: string; format: string }) {
      return `${EVIDENCE_BASE}/reports/completion${buildQuery(params as Record<string, QueryValue>)}`;
    },
    statusUrl(params: Record<string, QueryValue>) {
      return `${EVIDENCE_BASE}/reports/status${buildQuery(params)}`;
    },
  },
};
