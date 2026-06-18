import { chromium } from "playwright";

const EXEC = "/opt/pw-browsers/chromium-1194/chrome-linux/chrome";
const BASE = "http://localhost:3000";
const OUT = "/tmp/bc-shots";
const browser = await chromium.launch({ executablePath: EXEC });

async function login(page, email) {
  await page.goto(`${BASE}/login`, { waitUntil: "networkidle" });
  await page.fill('input[name="email"]', email);
  await page.fill('input[name="password"]', "password123");
  await page.locator("form button").last().click();
  await page.waitForURL("**/dashboard", { timeout: 15000 });
}

// Buyer buys a paid product (AntiFreecam, ৳299) → platform gets 10% = ৳29.9
{
  const ctx = await browser.newContext({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  await login(page, "buyer@brothercraft.dev");
  await page.goto(`${BASE}/product/antifreecam`, { waitUntil: "networkidle" });
  await page.getByRole("button", { name: /buy now/i }).click();
  await page.waitForURL("**/cart", { timeout: 10000 });
  await page.getByRole("button", { name: /pay with bkash/i }).click();
  await page.waitForURL("**/checkout/success**", { timeout: 15000 });
  console.log("paid purchase done");
  await ctx.close();
}

// Admin sees platform earnings + payout number
{
  const ctx = await browser.newContext({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  await login(page, "admin@brothercraft.dev");
  await page.goto(`${BASE}/admin`, { waitUntil: "networkidle" });
  await page.waitForTimeout(500);
  const text = await page.locator("body").innerText();
  const hasNumber = text.includes("01811799277");
  const hasEarnings = /29\.9|৳29/.test(text);
  await page.screenshot({ path: `${OUT}/admin-earnings.png`, fullPage: true });
  console.log("admin shows payout number:", hasNumber);
  console.log("admin shows ~10% earnings (৳29.9):", hasEarnings);
  await ctx.close();
}

await browser.close();
console.log("DONE");
