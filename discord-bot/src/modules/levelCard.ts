import { createCanvas, loadImage, type SKRSContext2D } from "@napi-rs/canvas";

export interface LevelCardData {
  username: string;
  avatarURL: string;
  level: number;
  rank?: number;
  currentXp: number;
  neededXp: number;
  totalXp: number;
  /** "levelup" shows a big "LEVEL UP!" banner; "rank" shows rank/xp stats. */
  mode: "levelup" | "rank";
  /** Accent / progress-bar color as a hex string, e.g. "#5865f2". */
  accent?: string;
}

const WIDTH = 934;
const HEIGHT = 282;

function roundedRect(ctx: SKRSContext2D, x: number, y: number, w: number, h: number, r: number) {
  ctx.beginPath();
  ctx.moveTo(x + r, y);
  ctx.arcTo(x + w, y, x + w, y + h, r);
  ctx.arcTo(x + w, y + h, x, y + h, r);
  ctx.arcTo(x, y + h, x, y, r);
  ctx.arcTo(x, y, x + w, y, r);
  ctx.closePath();
}

function abbreviate(n: number): string {
  if (n >= 1_000_000) return (n / 1_000_000).toFixed(1).replace(/\.0$/, "") + "M";
  if (n >= 1_000) return (n / 1_000).toFixed(1).replace(/\.0$/, "") + "K";
  return String(n);
}

/** Renders a level-up / rank card and returns a PNG buffer. */
export async function renderLevelCard(data: LevelCardData): Promise<Buffer> {
  const accent = data.accent ?? "#5865f2";
  const canvas = createCanvas(WIDTH, HEIGHT);
  const ctx = canvas.getContext("2d");

  // ── Background: dark gradient ───────────────────────────────────────────
  const bg = ctx.createLinearGradient(0, 0, WIDTH, HEIGHT);
  bg.addColorStop(0, "#1a1c2c");
  bg.addColorStop(1, "#2a1a3c");
  ctx.fillStyle = bg;
  ctx.fillRect(0, 0, WIDTH, HEIGHT);

  // Inner card panel with subtle border.
  ctx.fillStyle = "rgba(0,0,0,0.35)";
  roundedRect(ctx, 22, 22, WIDTH - 44, HEIGHT - 44, 28);
  ctx.fill();

  // ── Avatar (circular, with accent ring) ─────────────────────────────────
  const avX = 70;
  const avY = HEIGHT / 2;
  const avR = 88;

  ctx.save();
  ctx.beginPath();
  ctx.arc(avX + avR, avY, avR + 6, 0, Math.PI * 2);
  ctx.fillStyle = accent;
  ctx.fill();
  ctx.restore();

  try {
    const avatar = await loadImage(data.avatarURL);
    ctx.save();
    ctx.beginPath();
    ctx.arc(avX + avR, avY, avR, 0, Math.PI * 2);
    ctx.closePath();
    ctx.clip();
    ctx.drawImage(avatar, avX, avY - avR, avR * 2, avR * 2);
    ctx.restore();
  } catch {
    ctx.save();
    ctx.beginPath();
    ctx.arc(avX + avR, avY, avR, 0, Math.PI * 2);
    ctx.fillStyle = "#23272a";
    ctx.fill();
    ctx.fillStyle = "#ffffff";
    ctx.font = "bold 80px sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText((data.username[0] ?? "?").toUpperCase(), avX + avR, avY);
    ctx.restore();
  }

  const textX = avX + avR * 2 + 50;

  // ── Heading ─────────────────────────────────────────────────────────────
  ctx.textAlign = "left";
  ctx.textBaseline = "alphabetic";
  if (data.mode === "levelup") {
    ctx.fillStyle = accent;
    ctx.font = "bold 52px sans-serif";
    ctx.fillText("LEVEL UP!", textX, 110);
  } else {
    ctx.fillStyle = "#ffffff";
    ctx.font = "bold 46px sans-serif";
    ctx.fillText(data.username, textX, 100);
  }

  // Username (level-up mode) or rank info (rank mode).
  ctx.fillStyle = "#c7c9d1";
  ctx.font = "30px sans-serif";
  if (data.mode === "levelup") {
    ctx.fillText(data.username, textX, 150);
  }

  // Big level + rank, right side.
  ctx.textAlign = "right";
  ctx.fillStyle = "#ffffff";
  ctx.font = "bold 40px sans-serif";
  ctx.fillText(`LEVEL ${data.level}`, WIDTH - 60, 100);
  if (data.rank) {
    ctx.fillStyle = "#9aa0b5";
    ctx.font = "28px sans-serif";
    ctx.fillText(`RANK #${data.rank}`, WIDTH - 60, 145);
  }

  // ── XP progress bar ─────────────────────────────────────────────────────
  const barX = textX;
  const barW = WIDTH - barX - 60;
  const barY = 200;
  const barH = 34;
  const pct = Math.max(0, Math.min(1, data.neededXp > 0 ? data.currentXp / data.neededXp : 0));

  ctx.fillStyle = "rgba(255,255,255,0.12)";
  roundedRect(ctx, barX, barY, barW, barH, barH / 2);
  ctx.fill();

  if (pct > 0) {
    ctx.fillStyle = accent;
    roundedRect(ctx, barX, barY, Math.max(barH, barW * pct), barH, barH / 2);
    ctx.fill();
  }

  ctx.fillStyle = "#ffffff";
  ctx.font = "22px sans-serif";
  ctx.textAlign = "left";
  ctx.fillText(`${abbreviate(data.currentXp)} / ${abbreviate(data.neededXp)} XP`, barX + 4, barY + barH + 28);

  ctx.textAlign = "right";
  ctx.fillStyle = "#9aa0b5";
  ctx.fillText(`Total: ${abbreviate(data.totalXp)} XP`, barX + barW, barY + barH + 28);

  return canvas.toBuffer("image/png");
}
