import { getAccessToken } from "@/lib/auth/token";
import type { ApiError, ApiResponse } from "./types";

const BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL || "http://localhost:8080/api/v1";

/** Error thrown by the API client, carrying the backend's stable error code + HTTP status. */
export class ApiClientError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
    public readonly details?: { field: string; message: string }[],
  ) {
    super(message);
    this.name = "ApiClientError";
  }
}

interface RequestOptions {
  method?: string;
  body?: unknown;
  query?: Record<string, string | number | boolean | undefined | null>;
  signal?: AbortSignal;
}

function buildUrl(path: string, query?: RequestOptions["query"]): string {
  const url = new URL(BASE_URL + path);
  if (query) {
    Object.entries(query).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== "") url.searchParams.set(k, String(v));
    });
  }
  return url.toString();
}

/**
 * Core request helper. Attaches the bearer token when present, unwraps the standard
 * ApiResponse envelope, and maps error envelopes to {@link ApiClientError}.
 */
export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, query, signal } = options;
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  const token = getAccessToken();
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(buildUrl(path, query), {
    method,
    headers,
    body: body !== undefined ? JSON.stringify(body) : undefined,
    signal,
    cache: "no-store",
  });

  const text = await res.text();
  const payload = text ? JSON.parse(text) : null;

  if (!res.ok) {
    const err = payload as ApiError | null;
    throw new ApiClientError(
      res.status,
      err?.error?.code ?? "UNKNOWN",
      err?.error?.message ?? friendlyStatus(res.status),
      err?.error?.details,
    );
  }

  return (payload as ApiResponse<T>).data;
}

function friendlyStatus(status: number): string {
  switch (status) {
    case 400: return "The request could not be processed.";
    case 401: return "Please sign in to continue.";
    case 403: return "You do not have permission to perform this action.";
    case 404: return "The requested resource was not found.";
    case 409: return "This action conflicts with the current state.";
    case 422: return "Some fields failed validation.";
    default: return "Something went wrong. Please try again.";
  }
}
