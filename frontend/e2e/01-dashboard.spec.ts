import { test, expect, seedCalculation } from "./fixtures";

/**
 * Dashboard tests — /attainment
 */
test.describe("Attainment Dashboard", () => {

  test.beforeAll(async ({ request }) => {
    // Ensure data exists before running dashboard tests
    await seedCalculation(request);
  });

  test("page loads and shows title", async ({ page }) => {
    await page.goto("/attainment");
    await expect(page).toHaveTitle(/NBA Attainment/i);
    await expect(page.getByRole("heading", { name: /Attainment Dashboard/i })).toBeVisible();
  });

  test("navigation bar is visible with all links", async ({ page }) => {
    await page.goto("/attainment");
    await expect(page.getByRole("navigation")).toBeVisible();
    await expect(page.getByRole("link", { name: /PO Attainment/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /PSO Attainment/i })).toBeVisible();
    await expect(page.getByRole("link", { name: /Calculate/i })).toBeVisible();
  });

  test("summary cards are displayed after data loads", async ({ page }) => {
    await page.goto("/attainment");
    const cards = page.getByTestId("summary-cards");
    await expect(cards).toBeVisible({ timeout: 10_000 });
    // Should show at least Avg PO and Avg PSO cards
    await expect(page.getByText(/Avg PO Attainment/i)).toBeVisible();
    await expect(page.getByText(/Avg PSO Attainment/i)).toBeVisible();
  });

  test("PO attainment table is shown", async ({ page }) => {
    await page.goto("/attainment");
    const table = page.getByTestId("po-table");
    await expect(table).toBeVisible({ timeout: 10_000 });
    // PO1 should be in the table
    await expect(table.getByText("PO1")).toBeVisible();
  });

  test("PSO attainment table is shown", async ({ page }) => {
    await page.goto("/attainment");
    const table = page.getByTestId("pso-table");
    await expect(table).toBeVisible({ timeout: 10_000 });
    await expect(table.getByText("PSO1")).toBeVisible();
  });

  test("status badges are visible in PO table", async ({ page }) => {
    await page.goto("/attainment");
    await page.getByTestId("po-table").waitFor({ timeout: 10_000 });
    const badge = page.getByTestId("po-table").getByText(/Excellent|Good|Needs Improvement/).first();
    await expect(badge).toBeVisible();
  });

  test("academic year selector changes the view", async ({ page }) => {
    await page.goto("/attainment");
    const select = page.getByRole("combobox", { name: /academic year/i });
    await expect(select).toBeVisible();
    await select.selectOption("2022-23");
    // After changing year, the alert or empty state should show
    // (no data for 2022-23 in mock)
    await expect(page.getByText(/2022-23|No attainment records/i).first()).toBeVisible({ timeout: 5_000 });
  });

  test("root path redirects to /attainment", async ({ page }) => {
    await page.goto("/");
    await expect(page).toHaveURL(/\/attainment/);
  });
});
