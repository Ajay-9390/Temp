import { test, expect, seedCalculation } from "./fixtures";

/**
 * PO list page tests — /attainment/po
 */
test.describe("PO Attainment List", () => {

  test.beforeAll(async ({ request }) => {
    await seedCalculation(request);
  });

  test("navigating to PO list shows the page", async ({ page }) => {
    await page.goto("/attainment/po");
    await expect(page.getByTestId("po-list-page")).toBeVisible();
    await expect(page.getByRole("heading", { name: /PO Attainment/i })).toBeVisible();
  });

  test("PO table is visible with data", async ({ page }) => {
    await page.goto("/attainment/po");
    const table = page.getByTestId("po-table");
    await expect(table).toBeVisible({ timeout: 10_000 });
  });

  test("all 12 POs are shown", async ({ page }) => {
    await page.goto("/attainment/po");
    await page.getByTestId("po-table").waitFor({ timeout: 10_000 });
    const rows = page.getByTestId("po-table").locator("tbody tr");
    await expect(rows).toHaveCount(12);
  });

  test("PO table has Details links", async ({ page }) => {
    await page.goto("/attainment/po");
    await page.getByTestId("po-table").waitFor({ timeout: 10_000 });
    const link = page.getByTestId("po-detail-link-PO1");
    await expect(link).toBeVisible();
  });

  test("clicking Details link navigates to PO detail page", async ({ page }) => {
    await page.goto("/attainment/po");
    await page.getByTestId("po-table").waitFor({ timeout: 10_000 });
    await page.getByTestId("po-detail-link-PO1").click();
    await expect(page).toHaveURL(/\/attainment\/po\/.+/);
    await expect(page.getByTestId("po-detail-page")).toBeVisible({ timeout: 8_000 });
  });

  test("nav link highlights PO Attainment as active", async ({ page }) => {
    await page.goto("/attainment/po");
    const activeLink = page.getByRole("link", { name: /PO Attainment/i });
    await expect(activeLink).toHaveAttribute("aria-current", "page");
  });
});
