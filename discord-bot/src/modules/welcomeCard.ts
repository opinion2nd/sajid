import { createCanvas } from "@napi-rs/canvas";
import { roundedRect, loadAvatar } from "../util/canvas.js";

export interface WelcomeCardData {
  username: string;
  avatarURL: string;
  serverName: string;
  memberCount: number;
  /** "welcome" greets a new member; "goodbye" sees one off. */
  mode: "welcome" | "goodbye";
  /** Accent color as a hex string, e.g. "#5865f2". */
  accent?: string;
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

  // ── Background: dark gradient ───────────────────────────────────────────
  const bg = ctx.createLinearGradient(0, 0, WIDTH, HEIGHT);
  bg.addColorStop(0, "#15121f");
  bg.addColorStop(1, "#231a33");
  ctx.fillStyle = bg;
  ctx.fillRect(0, 0, WIDTH, HEIGHT);

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
  const sub =
    data.mode === "welcome"
      ? `You're the ${ordinal(data.memberCount)} member of ${data.serverName}!`
      : `${data.serverName} now has ${data.memberCount} members.`;
  ctx.fillText(sub, cx, 300);

  return canvas.toBuffer("image/png");
}
