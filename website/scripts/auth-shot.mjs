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

await page.goto(`${BASE}/login`, { waitUntil: "networkidle" });
await page.fill('input[name="email"]', "buyer@brothercraft.dev");
await page.fill('input[name="password"]', "password123");
await page.locator("form button").last().click();
await page.waitForURL("**/dashboard", { timeout: 15000 }).catch(() => {});
await page.waitForTimeout(1000);
await page.screenshot({ path: `${OUT}/dashboard-desktop.png`, fullPage: true });
console.log("dashboard ok:", page.url());

await page.goto(`${BASE}/dashboard/downloads`, { waitUntil: "networkidle" });
await page.waitForTimeout(600);
await page.screenshot({ path: `${OUT}/downloads-desktop.png`, fullPage: true });
console.log("downloads ok");

await browser.close();
