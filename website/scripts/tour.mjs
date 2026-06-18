import { chromium } from "playwright";
import { writeFileSync } from "fs";

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

// 1) Seller: request a payout + upload a product file
{
  const ctx = await browser.newContext({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  await login(page, "seller@brothercraft.dev");

  await page.goto(`${BASE}/seller/payouts`, { waitUntil: "networkidle" });
  await page.fill('input[name="amount"]', "100");
  await page.locator("form button", { hasText: /request payout/i }).click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: `${OUT}/seller-payouts.png`, fullPage: true });
  console.log("seller payouts + request ok");

  // Upload a product with a real file
  const tmp = "/tmp/sample-plugin.jar";
  writeFileSync(tmp, "PK fake jar bytes for brother craft demo");
  await page.goto(`${BASE}/seller/new`, { waitUntil: "networkidle" });
  await page.fill('input[name="title"]', "Test Upload Plugin");
  await page.fill('input[name="summary"]', "Uploaded via the seller flow");
  await page.selectOption('select[name="category"]', "plugins");
  await page.fill('input[name="price"]', "150");
  await page.setInputFiles('input[name="file"]', tmp);
  await page.locator("form button", { hasText: /create product/i }).click();
  await page.waitForURL("**/seller", { timeout: 15000 });
  await page.waitForTimeout(500);
  await page.screenshot({ path: `${OUT}/seller-dashboard.png`, fullPage: true });
  console.log("seller product upload ok");
  await ctx.close();
}

// 2) Buyer: open a dispute + Discord link code
{
  const ctx = await browser.newContext({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  await login(page, "buyer@brothercraft.dev");

  await page.goto(`${BASE}/dashboard/orders`, { waitUntil: "networkidle" });
  await page.fill('input[name="reason"]', "Plugin crashes on my server, need help or refund");
  await page.locator("form button", { hasText: /report a problem/i }).click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: `${OUT}/buyer-orders-dispute.png`, fullPage: true });
  console.log("buyer dispute ok");

  await page.goto(`${BASE}/dashboard/discord`, { waitUntil: "networkidle" });
  await page.locator("button", { hasText: /generate link code/i }).click();
  await page.waitForTimeout(800);
  await page.screenshot({ path: `${OUT}/buyer-discord.png`, fullPage: true });
  console.log("buyer discord ok");
  await ctx.close();
}

// 3) Admin: see payout + dispute queues
{
  const ctx = await browser.newContext({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  await login(page, "admin@brothercraft.dev");
  await page.goto(`${BASE}/admin`, { waitUntil: "networkidle" });
  await page.waitForTimeout(500);
  await page.screenshot({ path: `${OUT}/admin.png`, fullPage: true });
  console.log("admin ok");
  await ctx.close();
}

// 4) Home (logo)
{
  const ctx = await browser.newContext({ viewport: { width: 1366, height: 900 }, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  await page.goto(`${BASE}/`, { waitUntil: "networkidle" });
  await page.waitForTimeout(500);
  await page.screenshot({ path: `${OUT}/home-logo.png`, fullPage: false });
  console.log("home logo ok");
  await ctx.close();
}

await browser.close();
console.log("TOUR DONE");
