import { db } from "../db.js";

export interface Warning {
  id: number;
  guild_id: string;
  user_id: string;
  moderator_id: string;
  reason: string;
  created_at: number;
}

export function addWarning(guildId: string, userId: string, moderatorId: string, reason: string) {
  db.prepare(
    "INSERT INTO warnings (guild_id, user_id, moderator_id, reason, created_at) VALUES (?, ?, ?, ?, ?)"
  ).run(guildId, userId, moderatorId, reason, Date.now());
}

export function getWarnings(guildId: string, userId: string): Warning[] {
  return db
    .prepare("SELECT * FROM warnings WHERE guild_id = ? AND user_id = ? ORDER BY created_at DESC")
    .all(guildId, userId) as Warning[];
}

export function clearWarnings(guildId: string, userId: string): number {
  const result = db.prepare("DELETE FROM warnings WHERE guild_id = ? AND user_id = ?").run(guildId, userId);
  return result.changes;
}

// ── Automod ──────────────────────────────────────────────────────────────
const INVITE_REGEX = /(discord\.gg|discord(?:app)?\.com\/invite)\/[a-z0-9-]+/i;
const SPAM_WINDOW_MS = 8000;
const SPAM_MAX_REPEATS = 4;

const recentMessages = new Map<string, { content: string; timestamps: number[] }>();

export interface AutomodConfig {
  anti_invite: boolean;
  anti_caps: boolean;
  anti_spam: boolean;
  max_mentions: number;
}

export interface AutomodViolation {
  type: "invite" | "mentions" | "caps" | "spam";
  reason: string;
}

export function checkAutomod(
  guildId: string,
  userId: string,
  content: string,
  mentionCount: number,
  config: AutomodConfig
): AutomodViolation | null {
  if (config.anti_invite && INVITE_REGEX.test(content)) {
    return { type: "invite", reason: "Posting server invite links is not allowed." };
  }

  if (config.max_mentions > 0 && mentionCount > config.max_mentions) {
    return { type: "mentions", reason: `Mentioning too many users at once (${mentionCount}).` };
  }

  if (config.anti_caps && content.length >= 12) {
    const letters = content.replace(/[^a-zA-Z]/g, "");
    const upper = content.replace(/[^A-Z]/g, "");
    if (letters.length >= 10 && upper.length / letters.length > 0.7) {
      return { type: "caps", reason: "Excessive use of capital letters." };
    }
  }

  if (config.anti_spam) {
    const key = `${guildId}:${userId}`;
    const now = Date.now();
    const entry = recentMessages.get(key);
    if (entry && entry.content === content) {
      entry.timestamps = entry.timestamps.filter((t) => now - t < SPAM_WINDOW_MS);
      entry.timestamps.push(now);
      if (entry.timestamps.length >= SPAM_MAX_REPEATS) {
        recentMessages.set(key, { content, timestamps: [] });
        return { type: "spam", reason: "Sending duplicate messages too quickly." };
      }
    } else {
      recentMessages.set(key, { content, timestamps: [now] });
    }
  }

  return null;
}
