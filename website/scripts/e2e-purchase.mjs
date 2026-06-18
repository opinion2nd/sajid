import { chromium } from "playwright";

const EXEC = "/opt/pw-browsers/chromium-1194/chrome-linux/chrome";
const BASE = "http://localhost:3000";
const OUT = "/tmp/bc-shots";

const browser = await chromium.launch({ executablePath: EXEC });
const ctx = await browser.newContext({
  viewport: { width: 1366, height: 900 },
  deviceScaleFactor: 2,
});
const page = await ctx.newPage();

// Sign in
await page.goto(`${BASE}/login`, { waitUntil: "networkidle" });
await page.fill('input[name="email"]', "buyer@brothercraft.dev");
await page.fill('input[name="password"]', "password123");
await page.locator("form button").last().click();
await page.waitForURL("**/dashboard", { timeout: 15000 });

// Buy AntiFreecam (not previously owned)
await page.goto(`${BASE}/product/antifreecam`, { waitUntil: "networkidle" });
await page.getByRole("button", { name: /buy now/i }).click();
await page.waitForURL("**/cart", { timeout: 10000 });
await page.waitForTimeout(400);
await page.getByRole("button", { name: /pay with bkash/i }).click();

// Mock provider redirects through to the success page
await page.waitForURL("**/checkout/success**", { timeout: 15000 });
await page.waitForTimeout(800);
const bodyText = await page.locator("body").innerText();
const ok =
  bodyText.includes("Payment successful") && /AFC-[A-Z0-9-]+/.test(bodyText);
await page.screenshot({ path: `${OUT}/checkout-success.png`, fullPage: true });
console.log("PURCHASE E2E:", ok ? "PASS — license minted" : "CHECK output");
console.log("license shown:", (bodyText.match(/AFC-[A-Z0-9-]+/) || ["none"])[0]);

await browser.close();
