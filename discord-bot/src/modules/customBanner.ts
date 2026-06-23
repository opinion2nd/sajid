import fs from "node:fs";
import path from "node:path";

const BANNER_DIR = path.join(process.cwd(), "data", "banners");

// PNG chunk types our canvas renderer actually needs (or that are harmless metadata).
// Design tools like Canva embed private chunks (e.g. "caBX") that some PNG decoders
// mis-sniff as a different format entirely, so we strip anything not on this list.
const SAFE_PNG_CHUNKS = new Set([
  "IHDR", "PLTE", "tRNS", "IDAT", "IEND",
  "gAMA", "cHRM", "sRGB", "iCCP", "pHYs", "bKGD", "tIME",
]);

/** Drops non-standard/vendor PNG chunks (e.g. Canva's "caBX") that can break image decoders. */
function sanitizePng(buffer: Buffer): Buffer {
  const sig = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
  if (!buffer.subarray(0, 8).equals(sig)) return buffer;

  const kept: Buffer[] = [sig];
  let offset = 8;
  while (offset + 8 <= buffer.length) {
    const len = buffer.readUInt32BE(offset);
    const type = buffer.subarray(offset + 4, offset + 8).toString("ascii");
    const chunkEnd = offset + 8 + len + 4;
    if (chunkEnd > buffer.length) break;
    if (SAFE_PNG_CHUNKS.has(type)) kept.push(buffer.subarray(offset, chunkEnd));
    offset = chunkEnd;
    if (type === "IEND") break;
  }
  return Buffer.concat(kept);
}

/** Downloads a member-supplied banner image and saves it locally, keyed by guild + kind. */
export async function saveCustomBanner(guildId: string, kind: "welcome" | "leave", url: string): Promise<string> {
  if (!fs.existsSync(BANNER_DIR)) fs.mkdirSync(BANNER_DIR, { recursive: true });

  const res = await fetch(url);
  if (!res.ok) throw new Error(`banner fetch failed: ${res.status}`);
  const buffer = Buffer.from(await res.arrayBuffer());

  const ext = path.extname(new URL(url).pathname) || ".png";
  const finalBuffer = ext.toLowerCase() === ".png" ? sanitizePng(buffer) : buffer;

  const filePath = path.join(BANNER_DIR, `${guildId}-${kind}${ext}`);
  fs.writeFileSync(filePath, finalBuffer);
  return filePath;
}
