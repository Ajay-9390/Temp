import { test, expect, seedCalculation, API_BASE, MOCK_PROGRAM_ID, ACADEMIC_YEAR } from "./fixtures";

/**
 * PO detail / trace page tests — /attainment/po/[id]
 */
test.describe("PO Detail and Trace", () => {

  let poId: string;

  test.beforeAll(async ({ request }) => {
    await seedCalculation(request);
    // Get the PO1 record to have a stable ID for the tests
    const res = await request.get(
      `${API_BASE}/api/v1/attainment/po?programId=${MOCK_PROGRAM_ID}&academicYear=${ACADEMIC_YEAR}`
    );
    const data = await res.json() as Array<{ id: string; poCode: string }>;
    const po1 = data.find((r) => r.poCode === "PO1");
    if (!po1) throw new Error("PO1 not found in data");
    poId = po1.id;
  });

  test("PO detail page shows outcome code and description", async ({ page }) => {
    await page.goto(`/attainment/po/${poId}`);
    await expect(page.getByTestId("po-detail-page")).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText("PO1")).toBeVisible();
    await expect(page.getByText(/Engineering Knowledge/i)).toBeVisible();
  });

  test("attainment values are shown", async ({ page }) => {
    await page.goto(`/attainment/po/${poId}`);
    await expect(page.getByTestId("po-detail-page")).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText(/Direct Attainment/i)).toBeVisible();
    await expect(page.getByText(/Final Attainment/i)).toBeVisible();
  });

  test("calculation metadata is shown", async ({ page }) => {
    await page.goto(`/attainment/po/${poId}`);
    await expect(page.getByTestId("po-detail-page")).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText(/Calculation Details/i)).toBeVisible();
    await expect(page.getByText("WEIGHTED")).toBeVisible();
    await expect(page.getByText("v1")).toBeVisible();
    await expect(page.getByText(ACADEMIC_YEAR)).toBeVisible();
  });

  test("calculation trace section is visible", async ({ page }) => {
    await page.goto(`/attainment/po/${poId}`);
    await expect(page.getByTestId("po-detail-page")).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText(/Calculation Trace/i)).toBeVisible();
  });

  test("course breakdown is expandable", async ({ page }) => {
    await page.goto(`/attainment/po/${poId}`);
    await expect(page.getByTestId("po-detail-page")).toBeVisible({ timeout: 8_000 });
    // At least one course section should be visible
    const courseBtn = page.getByRole("button", { name: /Data Structures|OOP|Algorithm/i }).first();
    await expect(courseBtn).toBeVisible({ timeout: 5_000 });
    // Click to collapse
    await courseBtn.click();
    // Click again to expand
    await courseBtn.click();
    // CO table should be visible again
    await expect(page.getByText(/CO1|CO2|CO3/).first()).toBeVisible();
  });

  test("back link navigates to PO list", async ({ page }) => {
    await page.goto(`/attainment/po/${poId}`);
    await expect(page.getByTestId("po-detail-page")).toBeVisible({ timeout: 8_000 });
    await page.getByRole("link", { name: /Back to PO List/i }).click();
    await expect(page).toHaveURL("/attainment/po");
  });

  test("invalid ID shows error state", async ({ page }) => {
    await page.goto("/attainment/po/00000000-0000-0000-0000-000000000000");
    await expect(page.getByRole("alert")).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText(/not found|failed/i)).toBeVisible();
  });
});
