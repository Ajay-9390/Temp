import { expect, test } from "@playwright/test";

/**
 * Smoke tests for the Evidence Management UI. Require the backend and frontend to be running
 * (see README). These validate navigation and core page rendering.
 */

test("dashboard renders stat cards", async ({ page }) => {
  await page.goto("/evidence");
  await expect(page.getByRole("heading", { name: "Evidence Dashboard" })).toBeVisible();
  await expect(page.getByText("Total Evidence")).toBeVisible();
});

test("navigate to evidence list and filter", async ({ page }) => {
  await page.goto("/evidence/list");
  await expect(page.getByRole("heading", { name: "All Evidence" })).toBeVisible();
  await page.getByPlaceholder(/Title, description, filename/).fill("certificate");
});

test("open upload page and validate required title", async ({ page }) => {
  await page.goto("/evidence/upload");
  await expect(page.getByRole("heading", { name: "Upload Evidence" })).toBeVisible();
  await page.getByRole("button", { name: "Create Evidence" }).click();
  // Zod validation should surface a title error since no title/file were provided.
  await expect(page.getByText(/Title is required|choose a file/i)).toBeVisible();
});

test("gaps page loads required-evidence table", async ({ page }) => {
  await page.goto("/evidence/gaps");
  await expect(page.getByRole("heading", { name: "Evidence Gaps" })).toBeVisible();
  await expect(page.getByText("Required")).toBeVisible();
});

test("reports page exposes export buttons", async ({ page }) => {
  await page.goto("/evidence/reports");
  await expect(page.getByRole("heading", { name: "Evidence Reports" })).toBeVisible();
  await expect(page.getByRole("link", { name: /Excel/ }).first()).toBeVisible();
});
