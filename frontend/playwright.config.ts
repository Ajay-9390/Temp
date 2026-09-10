import { defineConfig, devices } from "@playwright/test";

/**
 * Playwright configuration for PO/PSO Attainment E2E tests.
 *
 * Tests run against a locally running Next.js dev server (port 3000).
 * The backend must be running on port 8080 with mock providers active.
 */
export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false, // sequential to avoid DB race conditions with shared backend
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: 1,
  reporter: [
    ["list"],
    ["html", { outputFolder: "playwright-report", open: "never" }],
  ],
  use: {
    baseURL: process.env.PLAYWRIGHT_BASE_URL || "http://localhost:3000",
    trace: "on-first-retry",
    screenshot: "only-on-failure",
    video: "retain-on-failure",
  },
  projects: [
    {
      name: "chromium",
      use: { ...devices["Desktop Chrome"] },
    },
  ],
  webServer: process.env.CI
    ? {
        command: "npm run start",
        port: 3000,
        reuseExistingServer: false,
        timeout: 120_000,
      }
    : undefined, // in dev, start the server manually
});
