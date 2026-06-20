import { loadImage, type SKRSContext2D } from "@napi-rs/canvas";

/** Draws a rounded rectangle path (call fill()/stroke() after). */
export function roundedRect(ctx: SKRSContext2D, x: number, y: number, w: number, h: number, r: number) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}

/**
 * Loads a Discord avatar reliably. Discord's CDN rejects requests without a
 * browser-like User-Agent, and @napi-rs/canvas's built-in URL loader doesn't
 * always send one — so we fetch the bytes ourselves first, then decode.
 */
export async function loadAvatar(url: string) {
  const res = await fetch(url, {
    headers: { "User-Agent": "Mozilla/5.0 (compatible; BrotherCraftBot/1.0; +https://discord.com)" },
  });
  if (!res.ok) throw new Error(`avatar fetch failed: ${res.status}`);
  const buffer = Buffer.from(await res.arrayBuffer());
  return loadImage(buffer);
}
