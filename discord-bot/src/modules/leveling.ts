import { db } from "../db.js";

const COOLDOWN_MS = 60_000;
const XP_MIN = 15;
const XP_MAX = 25;

const cooldowns = new Map<string, number>();

export function xpForLevel(level: number): number {
  return 5 * level * level + 50 * level + 100;
}

export function levelFromXp(totalXp: number): number {
  let level = 0;
  let remaining = totalXp;
  while (remaining >= xpForLevel(level)) {
    remaining -= xpForLevel(level);
    level++;
  }
  return level;
}

export function xpProgress(totalXp: number) {
  const level = levelFromXp(totalXp);
  let remaining = totalXp;
  for (let i = 0; i < level; i++) remaining -= xpForLevel(i);
  return { level, currentLevelXp: remaining, neededXp: xpForLevel(level) };
}

interface LevelRow {
  xp: number;
  level: number;
}

export function maybeAddXp(guildId: string, userId: string): { leveledUp: boolean; newLevel: number } | null {
  const key = `${guildId}:${userId}`;
  const now = Date.now();
  const last = cooldowns.get(key) ?? 0;
  if (now - last < COOLDOWN_MS) return null;
  cooldowns.set(key, now);

  const row = db
    .prepare("SELECT xp, level FROM levels WHERE guild_id = ? AND user_id = ?")
    .get(guildId, userId) as LevelRow | undefined;

  const gained = XP_MIN + Math.floor(Math.random() * (XP_MAX - XP_MIN + 1));
  const currentXp = row?.xp ?? 0;
  const newXp = currentXp + gained;
  const newLevel = levelFromXp(newXp);
  const oldLevel = row?.level ?? 0;

  if (row) {
    db.prepare(
      "UPDATE levels SET xp = ?, level = ?, last_message_at = ? WHERE guild_id = ? AND user_id = ?"
    ).run(newXp, newLevel, now, guildId, userId);
  } else {
    db.prepare(
      "INSERT INTO levels (guild_id, user_id, xp, level, last_message_at) VALUES (?, ?, ?, ?, ?)"
    ).run(guildId, userId, newXp, newLevel, now);
  }

  return { leveledUp: newLevel > oldLevel, newLevel };
}

export function getUserXp(guildId: string, userId: string): number {
  const row = db
    .prepare("SELECT xp FROM levels WHERE guild_id = ? AND user_id = ?")
    .get(guildId, userId) as { xp: number } | undefined;
  return row?.xp ?? 0;
}

export function getRank(guildId: string, userId: string) {
  const all = db
    .prepare("SELECT user_id, xp FROM levels WHERE guild_id = ? ORDER BY xp DESC")
    .all(guildId) as { user_id: string; xp: number }[];
  const idx = all.findIndex((r) => r.user_id === userId);
  if (idx === -1) return null;
  return { rank: idx + 1, total: all.length, xp: all[idx].xp };
}

export function getLeaderboard(guildId: string, limit = 10) {
  return db
    .prepare("SELECT user_id, xp, level FROM levels WHERE guild_id = ? ORDER BY xp DESC LIMIT ?")
    .all(guildId, limit) as { user_id: string; xp: number; level: number }[];
}
