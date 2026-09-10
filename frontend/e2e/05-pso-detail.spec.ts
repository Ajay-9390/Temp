import { test, expect, seedCalculation, API_BASE, MOCK_PROGRAM_ID, ACADEMIC_YEAR } from "./fixtures";

/**
 * PSO detail / trace page tests — /attainment/pso/[id]
 */
test.describe("PSO Detail and Trace", () => {

  let psoId: string;

  test.beforeAll(async ({ request }) => {
    await seedCalculation(request);
    const res = await request.get(
      `${API_BASE}/api/v1/attainment/pso?programId=${MOCK_PROGRAM_ID}&academicYear=${ACADEMIC_YEAR}`
    );
    const data = await res.json() as Array<{ id: string; psoCode: string }>;
    const pso1 = data.find((r) => r.psoCode === "PSO1");
    if (!pso1) throw new Error("PSO1 not found in data");
    psoId = pso1.id;
  });

  test("PSO detail page shows outcome code and description", async ({ page }) => {
    await page.goto(`/attainment/pso/${psoId}`);
    await expect(page.getByTestId("pso-detail-page")).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText("PSO1")).toBeVisible();
    await expect(page.getByText(/Software Engineering/i)).toBeVisible();
  });

  test("calculation trace is shown with course breakdown", async ({ page }) => {
    await page.goto(`/attainment/pso/${psoId}`);
    await expect(page.getByTestId("pso-detail-page")).toBeVisible({ timeout: 8_000 });
    await expect(page.getByText(/Calculation Trace/i)).toBeVisible();
    // Course sections should be present
    const courseBtns = page.getByRole("button", { name: /Data Structures|OOP|Algorithm/i });
    await expect(courseBtns.first()).toBeVisible({ timeout: 5_000 });
  });

  test("back link navigates to PSO list", async ({ page }) => {
    await page.goto(`/attainment/pso/${psoId}`);
    await expect(page.getByTestId("pso-detail-page")).toBeVisible({ timeout: 8_000 });
    await page.getByRole("link", { name: /Back to PSO List/i }).click();
    await expect(page).toHaveURL("/attainment/pso");
  });
});
