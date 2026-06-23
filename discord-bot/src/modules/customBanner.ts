import fs from "node:fs";
import path from "node:path";

const BANNER_DIR = path.join(process.cwd(), "data", "banners");

/** Downloads a member-supplied banner image and saves it locally, keyed by guild + kind. */
export async function saveCustomBanner(guildId: string, kind: "welcome" | "leave", url: string): Promise<string> {
  if (!fs.existsSync(BANNER_DIR)) fs.mkdirSync(BANNER_DIR, { recursive: true });

  const res = await fetch(url);
  if (!res.ok) throw new Error(`banner fetch failed: ${res.status}`);
  const buffer = Buffer.from(await res.arrayBuffer());

  const ext = path.extname(new URL(url).pathname) || ".png";
  const filePath = path.join(BANNER_DIR, `${guildId}-${kind}${ext}`);
  fs.writeFileSync(filePath, buffer);
  return filePath;
}
