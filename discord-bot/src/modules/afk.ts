import { db } from "../db.js";

export interface AfkEntry {
  guild_id: string;
  user_id: string;
  reason: string;
  set_at: number;
}

export function setAfk(guildId: string, userId: string, reason: string) {
  db.prepare(
    "INSERT INTO afk (guild_id, user_id, reason, set_at) VALUES (?, ?, ?, ?) ON CONFLICT(guild_id, user_id) DO UPDATE SET reason = excluded.reason, set_at = excluded.set_at"
  ).run(guildId, userId, reason, Date.now());
}

export function getAfk(guildId: string, userId: string): AfkEntry | undefined {
  return db.prepare("SELECT * FROM afk WHERE guild_id = ? AND user_id = ?").get(guildId, userId) as AfkEntry | undefined;
}

export function clearAfk(guildId: string, userId: string): boolean {
  const result = db.prepare("DELETE FROM afk WHERE guild_id = ? AND user_id = ?").run(guildId, userId);
  return result.changes > 0;
}
