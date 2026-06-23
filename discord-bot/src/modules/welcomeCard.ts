import fs from "node:fs";
import { createCanvas, loadImage } from "@napi-rs/canvas";
import { roundedRect, loadAvatar, drawImageCover } from "../util/canvas.js";

export interface WelcomeCardData {
  username: string;
  avatarURL: string;
  serverName: string;
  memberCount: number;
  /** "welcome" greets a new member; "goodbye" sees one off. */
  mode: "welcome" | "goodbye";
  /** Accent color as a hex string, e.g. "#5865f2". */
  accent?: string;
  /** Local path to a member-supplied background image; falls back to the gradient if unset or unreadable. */
  backgroundImagePath?: string | null;
}

const WIDTH = 934;
const HEIGHT = 320;

function ordinal(n: number): string {
  const s = ["th", "st", "nd", "rd"];
  const v = n % 100;
  return n + (s[(v - 20) % 10] ?? s[v] ?? s[0]);
}

/** Renders a welcome / goodbye banner and returns a PNG buffer. */
export async function renderWelcomeCard(data: WelcomeCardData): Promise<Buffer> {
  const accent = data.accent ?? (data.mode === "welcome" ? "#5865f2" : "#ed4245");
  const canvas = createCanvas(WIDTH, HEIGHT);
  const ctx = canvas.getContext("2d");

  // ── Background: custom image if provided, else dark gradient ───────────
  let customBackgroundDrawn = false;
  if (data.backgroundImagePath) {
    try {
      const img = await loadImage(fs.readFileSync(data.backgroundImagePath));
      drawImageCover(ctx, img, 0, 0, WIDTH, HEIGHT);
      customBackgroundDrawn = true;
    } catch {
      // Fall through to the gradient background below.
    }
  }
  if (!customBackgroundDrawn) {
    const bg = ctx.createLinearGradient(0, 0, WIDTH, HEIGHT);
    bg.addColorStop(0, "#15121f");
    bg.addColorStop(1, "#231a33");
    ctx.fillStyle = bg;
    ctx.fillRect(0, 0, WIDTH, HEIGHT);
  }

  const sub =
    data.mode === "welcome"
      ? `You're the ${ordinal(data.memberCount)} member of ${data.serverName}!`
      : `${data.serverName} now has ${data.memberCount} members.`;

  if (customBackgroundDrawn) {
    // The custom artwork already carries its own branding/title, so this layout
    // only adds a slim bottom bar with the avatar + username + member count —
    // no redundant "WELCOME"/"GOODBYE" title drawn over the user's design.
    const barH = 92;
    const barY = HEIGHT - barH;
    ctx.fillStyle = "rgba(0,0,0,0.55)";
    ctx.fillRect(0, barY, WIDTH, barH);

    const avR = 32;
    const avX = 64;
    const avY = barY + barH / 2;

    ctx.beginPath();
    ctx.arc(avX, avY, avR + 4, 0, Math.PI * 2);
    ctx.fillStyle = accent;
    ctx.fill();

    try {
      const avatar = await loadAvatar(data.avatarURL);
      ctx.save();
      ctx.beginPath();
      ctx.arc(avX, avY, avR, 0, Math.PI * 2);
      ctx.closePath();
      ctx.clip();
      ctx.drawImage(avatar, avX - avR, avY - avR, avR * 2, avR * 2);
      ctx.restore();
    } catch {
      ctx.save();
      ctx.beginPath();
      ctx.arc(avX, avY, avR, 0, Math.PI * 2);
      ctx.fillStyle = "#23272a";
      ctx.fill();
      ctx.fillStyle = "#ffffff";
      ctx.font = "bold 28px sans-serif";
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText((data.username[0] ?? "?").toUpperCase(), avX, avY);
      ctx.restore();
    }

    ctx.textAlign = "left";
    ctx.fillStyle = "#ffffff";
    ctx.font = "bold 26px sans-serif";
    ctx.textBaseline = "alphabetic";
    ctx.fillText(data.username, avX + avR + 22, avY - 4);

    ctx.fillStyle = "#d6d9e6";
    ctx.font = "18px sans-serif";
    ctx.fillText(sub, avX + avR + 22, avY + 24);

    return canvas.toBuffer("image/png");
  }

  // Glow behind the avatar.
  const glow = ctx.createRadialGradient(WIDTH / 2, 110, 10, WIDTH / 2, 110, 220);
  glow.addColorStop(0, accent + "55");
  glow.addColorStop(1, "transparent");
  ctx.fillStyle = glow;
  ctx.fillRect(0, 0, WIDTH, HEIGHT);

  // Inner panel.
  ctx.fillStyle = "rgba(0,0,0,0.30)";
  roundedRect(ctx, 22, 22, WIDTH - 44, HEIGHT - 44, 28);
  ctx.fill();

  // ── Avatar (centered, circular, accent ring) ────────────────────────────
  const cx = WIDTH / 2;
  const avY = 112;
  const avR = 72;

  ctx.beginPath();
  ctx.arc(cx, avY, avR + 6, 0, Math.PI * 2);
  ctx.fillStyle = accent;
  ctx.fill();

  try {
    const avatar = await loadAvatar(data.avatarURL);
    ctx.save();
    ctx.beginPath();
    ctx.arc(cx, avY, avR, 0, Math.PI * 2);
    ctx.closePath();
    ctx.clip();
    ctx.drawImage(avatar, cx - avR, avY - avR, avR * 2, avR * 2);
    ctx.restore();
  } catch {
    ctx.save();
    ctx.beginPath();
    ctx.arc(cx, avY, avR, 0, Math.PI * 2);
    ctx.fillStyle = "#23272a";
    ctx.fill();
    ctx.fillStyle = "#ffffff";
    ctx.font = "bold 64px sans-serif";
    ctx.textAlign = "center";
    ctx.textBaseline = "middle";
    ctx.fillText((data.username[0] ?? "?").toUpperCase(), cx, avY);
    ctx.restore();
  }

  // ── Text ─────────────────────────────────────────────────────────────────
  ctx.textAlign = "center";
  ctx.textBaseline = "alphabetic";

  ctx.fillStyle = accent;
  ctx.font = "bold 50px sans-serif";
  ctx.fillText(data.mode === "welcome" ? "WELCOME" : "GOODBYE", cx, 228);

  ctx.fillStyle = "#ffffff";
  ctx.font = "bold 34px sans-serif";
  ctx.fillText(data.username, cx, 268);

  ctx.fillStyle = "#9aa0b5";
  ctx.font = "24px sans-serif";
  ctx.fillText(sub, cx, 300);

  return canvas.toBuffer("image/png");
}
