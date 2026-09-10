import { test, expect, seedCalculation } from "./fixtures";

/**
 * PSO list page tests — /attainment/pso
 */
test.describe("PSO Attainment List", () => {

  test.beforeAll(async ({ request }) => {
    await seedCalculation(request);
  });

  test("navigating to PSO list shows the page", async ({ page }) => {
    await page.goto("/attainment/pso");
    await expect(page.getByTestId("pso-list-page")).toBeVisible();
    await expect(page.getByRole("heading", { name: /PSO Attainment/i })).toBeVisible();
  });

  test("PSO table is visible with 3 PSOs", async ({ page }) => {
    await page.goto("/attainment/pso");
    await page.getByTestId("pso-table").waitFor({ timeout: 10_000 });
    const rows = page.getByTestId("pso-table").locator("tbody tr");
    await expect(rows).toHaveCount(3);
  });

  test("PSO table has Details links", async ({ page }) => {
    await page.goto("/attainment/pso");
    await page.getByTestId("pso-table").waitFor({ timeout: 10_000 });
    const link = page.getByTestId("pso-detail-link-PSO1");
    await expect(link).toBeVisible();
  });

  test("clicking PSO Details link navigates to PSO detail page", async ({ page }) => {
    await page.goto("/attainment/pso");
    await page.getByTestId("pso-table").waitFor({ timeout: 10_000 });
    await page.getByTestId("pso-detail-link-PSO1").click();
    await expect(page).toHaveURL(/\/attainment\/pso\/.+/);
    await expect(page.getByTestId("pso-detail-page")).toBeVisible({ timeout: 8_000 });
  });
});
