import type {
  POAttainmentCalculationRequest,
  PSOAttainmentCalculationRequest,
  CombinedAttainmentCalculationRequest,
  POAttainmentResponse,
  PSOAttainmentResponse,
  CombinedAttainmentResponse,
  AttainmentSummaryResponse,
  POAttainmentTraceResponse,
  PSOAttainmentTraceResponse,
} from "./types";

const BASE = "/api/v1/attainment";

async function request<T>(
  url: string,
  options?: RequestInit
): Promise<T> {
  const res = await fetch(url, {
    headers: { "Content-Type": "application/json" },
    ...options,
  });

  if (!res.ok) {
    const error = await res.json().catch(() => ({
      status: res.status,
      error: "UNKNOWN",
      message: res.statusText,
      path: url,
    }));
    throw error;
  }

  return res.json() as Promise<T>;
}

// ─── Calculation endpoints ────────────────────────────────────────────────────

export function calculatePOAttainment(
  body: POAttainmentCalculationRequest
): Promise<POAttainmentResponse[]> {
  return request(`${BASE}/po/calculate`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function calculatePSOAttainment(
  body: PSOAttainmentCalculationRequest
): Promise<PSOAttainmentResponse[]> {
  return request(`${BASE}/pso/calculate`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

export function calculateAllAttainment(
  body: CombinedAttainmentCalculationRequest
): Promise<CombinedAttainmentResponse> {
  return request(`${BASE}/calculate`, {
    method: "POST",
    body: JSON.stringify(body),
  });
}

// ─── Query endpoints ──────────────────────────────────────────────────────────

export function getPOAttainments(
  programId: string,
  academicYear?: string
): Promise<POAttainmentResponse[]> {
  const params = new URLSearchParams({ programId });
  if (academicYear) params.set("academicYear", academicYear);
  return request(`${BASE}/po?${params}`);
}

export function getPSOAttainments(
  programId: string,
  academicYear?: string
): Promise<PSOAttainmentResponse[]> {
  const params = new URLSearchParams({ programId });
  if (academicYear) params.set("academicYear", academicYear);
  return request(`${BASE}/pso?${params}`);
}

export function getPOAttainmentById(id: string): Promise<POAttainmentResponse> {
  return request(`${BASE}/po/${id}`);
}

export function getPSOAttainmentById(id: string): Promise<PSOAttainmentResponse> {
  return request(`${BASE}/pso/${id}`);
}

export function getPOTrace(id: string): Promise<POAttainmentTraceResponse> {
  return request(`${BASE}/po/${id}/trace`);
}

export function getPSOTrace(id: string): Promise<PSOAttainmentTraceResponse> {
  return request(`${BASE}/pso/${id}/trace`);
}

export function getAttainmentSummary(
  programId: string,
  academicYear?: string
): Promise<AttainmentSummaryResponse> {
  const params = new URLSearchParams({ programId });
  if (academicYear) params.set("academicYear", academicYear);
  return request(`${BASE}/summary?${params}`);
}
