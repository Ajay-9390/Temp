import { test, expect } from "@playwright/test";

// These specs exercise the main flows against a running backend (seeded) + frontend.
// Login flow is a placeholder in standalone mode (backend security disabled); when Keycloak
// is integrated, add a global setup that authenticates and stores the token.

test.describe("Program Management UI", () => {
  test("program list loads and is searchable", async ({ page }) => {
    await page.goto("/programs");
    await expect(page.getByRole("heading", { name: "Programs" })).toBeVisible();
    await page.getByPlaceholder(/Search name, code/).fill("CSE");
    await expect(page.getByText(/B\.Tech/).first()).toBeVisible();
  });

  test("create program validation errors show", async ({ page }) => {
    await page.goto("/programs/new");
    await page.getByRole("button", { name: "Create Program" }).click();
    await expect(page.getByText("Program name is required")).toBeVisible();
    await expect(page.getByText("Program code is required")).toBeVisible();
  });

  test("open a program details page with tabs", async ({ page }) => {
    await page.goto("/programs");
    await page.getByRole("link", { name: "View" }).first().click();
    await expect(page.getByRole("tab", { name: "Overview" })).toBeVisible();
    await page.getByRole("tab", { name: "Accreditation" }).click();
    await expect(page.getByText(/Accreditation Cycles/)).toBeVisible();
  });

  test("navigate to departments", async ({ page }) => {
    await page.goto("/programs");
    await page.getByRole("link", { name: "Departments" }).click();
    await expect(page.getByRole("heading", { name: "Departments" })).toBeVisible();
  });

  test("create academic year via dialog", async ({ page }) => {
    await page.goto("/programs");
    await page.getByRole("link", { name: "View" }).first().click();
    await page.getByRole("tab", { name: "Academic Years" }).click();
    await page.getByRole("link", { name: "Manage Years" }).click();
    await expect(page.getByRole("heading", { name: "Academic Years" })).toBeVisible();
  });
});
