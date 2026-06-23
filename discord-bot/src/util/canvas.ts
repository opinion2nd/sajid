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

/** Draws an image scaled+cropped to fill a w x h box (like CSS `object-fit: cover`). */
export function drawImageCover(
  ctx: SKRSContext2D,
  img: { width: number; height: number },
  x: number,
  y: number,
  w: number,
  h: number,
) {
  const imgRatio = img.width / img.height;
  const boxRatio = w / h;
  let drawWidth: number, drawHeight: number, offsetX: number, offsetY: number;

  if (imgRatio > boxRatio) {
    drawHeight = h;
    drawWidth = h * imgRatio;
    offsetX = x - (drawWidth - w) / 2;
    offsetY = y;
  } else {
    drawWidth = w;
    drawHeight = w / imgRatio;
    offsetX = x;
    offsetY = y - (drawHeight - h) / 2;
  }

  ctx.save();
  ctx.beginPath();
  ctx.rect(x, y, w, h);
  ctx.clip();
  // @napi-rs/canvas's drawImage typing wants its own Image type; this helper is generic over
  // anything with width/height so it also accepts the result of loadImage() at call sites.
  ctx.drawImage(img as any, offsetX, offsetY, drawWidth, drawHeight);
  ctx.restore();
}
