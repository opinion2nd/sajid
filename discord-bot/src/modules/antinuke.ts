import { db } from "../db.js";

interface ActionRecord {
  timestamps: number[];
}

const actions = new Map<string, ActionRecord>();

// ── Anti-nuke whitelist ────────────────────────────────────────────────────
// Whitelisted users are fully trusted and are never punished by anti-nuke.

export function addNukeWhitelist(guildId: string, userId: string, addedBy: string) {
  db.prepare(
    "INSERT OR REPLACE INTO nuke_whitelist (guild_id, user_id, added_by, added_at) VALUES (?, ?, ?, ?)"
  ).run(guildId, userId, addedBy, Date.now());
}

export function removeNukeWhitelist(guildId: string, userId: string): boolean {
  const result = db.prepare("DELETE FROM nuke_whitelist WHERE guild_id = ? AND user_id = ?").run(guildId, userId);
  return result.changes > 0;
}

export function listNukeWhitelist(guildId: string): string[] {
  const rows = db.prepare("SELECT user_id FROM nuke_whitelist WHERE guild_id = ?").all(guildId) as { user_id: string }[];
  return rows.map((r) => r.user_id);
}

export function isNukeWhitelisted(guildId: string, userId: string): boolean {
  const row = db.prepare("SELECT 1 FROM nuke_whitelist WHERE guild_id = ? AND user_id = ?").get(guildId, userId);
  return Boolean(row);
}

/** Records a destructive action by an executor and returns true if it exceeds the nuke threshold. */
export function recordActionAndCheckNuke(
  guildId: string,
  executorId: string,
  thresholdCount: number,
  windowSeconds: number
): boolean {
  const now = Date.now();
  const windowMs = windowSeconds * 1000;
  const key = `${guildId}:${executorId}`;
  const record = actions.get(key) ?? { timestamps: [] };
  record.timestamps = record.timestamps.filter((t) => now - t < windowMs);
  record.timestamps.push(now);
  actions.set(key, record);
  return record.timestamps.length >= thresholdCount;
}

export function resetNukeTracking(guildId: string, executorId: string) {
  actions.delete(`${guildId}:${executorId}`);
}
