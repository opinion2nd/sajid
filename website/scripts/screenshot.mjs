import { chromium } from "playwright";
import { mkdirSync } from "fs";

const EXEC =
  process.env.CHROME_BIN ||
  "/opt/pw-browsers/chromium-1194/chrome-linux/chrome";
const BASE = "http://localhost:3000";
const OUT = "/tmp/bc-shots";
mkdirSync(OUT, { recursive: true });

const desktop = { width: 1366, height: 900 };
const mobile = { width: 390, height: 844 };

const pages = [
  { name: "home", path: "/" },
  { name: "browse", path: "/browse" },
  { name: "product", path: "/product/pillars-minigame" },
  { name: "login", path: "/login" },
  { name: "store", path: "/store/thewindows" },
  { name: "docs", path: "/docs/license-api" },
];

const browser = await chromium.launch({ executablePath: EXEC });

async function shoot(viewport, suffix) {
  const ctx = await browser.newContext({ viewport, deviceScaleFactor: 2 });
  const page = await ctx.newPage();
  for (const p of pages) {
    await page.goto(`${BASE}${p.path}`, { waitUntil: "networkidle" });
    await page.waitForTimeout(400);
    await page.screenshot({
      path: `${OUT}/${p.name}-${suffix}.png`,
      fullPage: true,
    });
    console.log(`shot ${p.name}-${suffix}`);
  }
  await ctx.close();
}

await shoot(desktop, "desktop");
await shoot(mobile, "mobile");

// Authenticated flow: log in as buyer and capture the dashboard.
const ctx = await browser.newContext({ viewport: desktop, deviceScaleFactor: 2 });
const page = await ctx.newPage();
await page.goto(`${BASE}/login`, { waitUntil: "networkidle" });
await page.fill('input[name="email"]', "buyer@brothercraft.dev");
await page.fill('input[name="password"]', "password123");
await page.click('button[type="submit"]');
await page.waitForURL("**/dashboard", { timeout: 15000 }).catch(() => {});
await page.waitForTimeout(800);
await page.screenshot({ path: `${OUT}/dashboard-desktop.png`, fullPage: true });
console.log("shot dashboard-desktop");
await page.goto(`${BASE}/dashboard/downloads`, { waitUntil: "networkidle" });
await page.waitForTimeout(500);
await page.screenshot({ path: `${OUT}/downloads-desktop.png`, fullPage: true });
console.log("shot downloads-desktop");
await ctx.close();

await browser.close();
console.log("done");
