import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright config for Evidence Management UI smoke/e2e tests.
 * Run the backend + `npm run dev` first, then `npm run test:e2e`.
 */
export default defineConfig({
  testDir: "./tests/e2e",
  timeout: 30_000,
  fullyParallel: true,
  reporter: "list",
  use: {
    baseURL: process.env.E2E_BASE_URL || "http://localhost:3000",
    trace: "on-first-retry",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
});
