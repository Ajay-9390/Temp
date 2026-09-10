import { test, expect, MOCK_PROGRAM_ID, ACADEMIC_YEAR } from "./fixtures";

/**
 * Calculation screen tests — /attainment/calculate
 */
test.describe("Calculation Screen", () => {

  test.beforeEach(async ({ page }) => {
    await page.goto("/attainment/calculate");
    await expect(page.getByTestId("calculate-page")).toBeVisible();
  });

  test("page shows calculation form", async ({ page }) => {
    await expect(page.getByRole("heading", { name: /Calculate Attainment/i })).toBeVisible();
    await expect(page.getByTestId("input-program-id")).toBeVisible();
    await expect(page.getByTestId("input-academic-year")).toBeVisible();
    await expect(page.getByTestId("select-method")).toBeVisible();
    await expect(page.getByTestId("input-version")).toBeVisible();
  });

  test("form is pre-filled with mock data defaults", async ({ page }) => {
    const programIdInput = page.getByTestId("input-program-id");
    await expect(programIdInput).toHaveValue(MOCK_PROGRAM_ID);
    const yearInput = page.getByTestId("input-academic-year");
    await expect(yearInput).toHaveValue(ACADEMIC_YEAR);
  });

  test("mode selector buttons are visible", async ({ page }) => {
    await expect(page.getByTestId("mode-po")).toBeVisible();
    await expect(page.getByTestId("mode-pso")).toBeVisible();
    await expect(page.getByTestId("mode-all")).toBeVisible();
  });

  test("selecting PO mode updates the calculate button label", async ({ page }) => {
    await page.getByTestId("mode-po").click();
    await expect(page.getByTestId("btn-calculate")).toContainText(/Calculate PO/i);
  });

  test("selecting PSO mode updates the calculate button label", async ({ page }) => {
    await page.getByTestId("mode-pso").click();
    await expect(page.getByTestId("btn-calculate")).toContainText(/Calculate PSO/i);
  });

  test("calculate ALL shows loading state then results", async ({ page }) => {
    // Make sure ALL mode is active
    await page.getByTestId("mode-all").click();
    await page.getByTestId("btn-calculate").click();

    // Button should show loading state
    await expect(page.getByTestId("btn-calculate")).toBeDisabled({ timeout: 2_000 });

    // Wait for success
    await expect(page.getByText(/Calculation Complete/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText(/12 PO records saved/i)).toBeVisible();
    await expect(page.getByText(/3 PSO records saved/i)).toBeVisible();
  });

  test("PO-only calculation shows PO results table", async ({ page }) => {
    await page.getByTestId("mode-po").click();
    await page.getByTestId("btn-calculate").click();
    await expect(page.getByText(/Calculation Complete/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText("PO Results")).toBeVisible();
    await expect(page.getByText("PSO Results")).not.toBeVisible();
  });

  test("PSO-only calculation shows PSO results table", async ({ page }) => {
    await page.getByTestId("mode-pso").click();
    await page.getByTestId("btn-calculate").click();
    await expect(page.getByText(/Calculation Complete/i)).toBeVisible({ timeout: 15_000 });
    await expect(page.getByText("PSO Results")).toBeVisible();
    await expect(page.getByText("PO Results")).not.toBeVisible();
  });

  test("empty program ID shows validation error", async ({ page }) => {
    await page.getByTestId("input-program-id").fill("");
    await page.getByTestId("btn-calculate").click();
    await expect(page.getByRole("alert")).toBeVisible({ timeout: 3_000 });
    await expect(page.getByText(/valid UUID/i)).toBeVisible();
  });

  test("invalid academic year format shows validation error", async ({ page }) => {
    await page.getByTestId("input-academic-year").fill("badyear");
    await page.getByTestId("btn-calculate").click();
    await expect(page.getByText(/Format: YYYY-YY/i)).toBeVisible({ timeout: 3_000 });
  });

  test("unknown program ID shows API error", async ({ page }) => {
    await page.getByTestId("input-program-id").fill("00000000-0000-0000-0000-000000000000");
    await page.getByTestId("btn-calculate").click();
    await expect(page.getByText(/Calculation Failed/i)).toBeVisible({ timeout: 12_000 });
  });
});
