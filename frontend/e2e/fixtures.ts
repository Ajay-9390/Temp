import { test as base, expect } from "@playwright/test";

/**
 * Shared fixture: navigate to /attainment and wait for the page to stabilise
 * before each test that needs a loaded dashboard.
 */
export const test = base.extend({});
export { expect };

/** Mock program UUID used by MockDataConstants in the backend. */
export const MOCK_PROGRAM_ID = "11111111-0000-0000-0000-000000000001";
export const ACADEMIC_YEAR   = "2023-24";
export const API_BASE        = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

/**
 * Trigger a combined calculation via the API so the UI has data to show.
 * Called in test setup where fresh data is needed.
 */
export async function seedCalculation(request: import("@playwright/test").APIRequestContext) {
  const res = await request.post(`${API_BASE}/api/v1/attainment/calculate`, {
    data: {
      programId:          MOCK_PROGRAM_ID,
      academicYear:       ACADEMIC_YEAR,
      calculationMethod:  "WEIGHTED",
      calculationVersion: "v1",
    },
  });
  if (!res.ok()) {
    throw new Error(`Seed calculation failed: ${res.status()} ${await res.text()}`);
  }
  return res.json();
}
