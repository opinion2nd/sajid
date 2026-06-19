import Database from "better-sqlite3";
import path from "node:path";

const dbPath = path.resolve(process.cwd(), process.env.BOT_DB_PATH || "../data/bot.sqlite3");
export const db = new Database(dbPath, { fileMustExist: true });
db.pragma("journal_mode = WAL");

export interface GuildConfig {
  guild_id: string;
  mod_log_channel: string | null;
  welcome_channel: string | null;
  welcome_message: string | null;
  leave_channel: string | null;
  leave_message: string | null;
  levelup_channel: string | null;
  ticket_category: string | null;
  ticket_log_channel: string | null;
  ticket_support_role: string | null;
  automod_enabled: number;
  automod_anti_invite: number;
  automod_anti_caps: number;
  automod_anti_spam: number;
  automod_max_mentions: number;
  suggestion_channel: string | null;
  verify_role: string | null;
  verify_channel: string | null;
  anti_raid_enabled: number;
  raid_join_threshold: number;
  raid_window_seconds: number;
  raid_account_age_days: number;
  anti_nuke_enabled: number;
  nuke_threshold: number;
  nuke_window_seconds: number;
  anti_ghostping_enabled: number;
}

export interface Ticket {
  id: number;
  guild_id: string;
  channel_id: string;
  user_id: string;
  status: string;
  created_at: number;
  closed_at: number | null;
}

export interface Level {
  guild_id: string;
  user_id: string;
  xp: number;
  level: number;
  last_message_at: number;
}

export interface Warning {
  id: number;
  guild_id: string;
  user_id: string;
  moderator_id: string;
  reason: string;
  created_at: number;
}

export function getGuildConfig(guildId: string): GuildConfig {
  let row = db.prepare("SELECT * FROM guild_config WHERE guild_id = ?").get(guildId) as GuildConfig | undefined;
  if (!row) {
    db.prepare("INSERT INTO guild_config (guild_id) VALUES (?)").run(guildId);
    row = db.prepare("SELECT * FROM guild_config WHERE guild_id = ?").get(guildId) as GuildConfig;
  }
  return row;
}

export function updateGuildConfig(guildId: string, fields: Record<string, string | number | null>) {
  getGuildConfig(guildId);
  const keys = Object.keys(fields);
  if (keys.length === 0) return;
  const sets = keys.map((k) => `${k} = ?`).join(", ");
  const values = keys.map((k) => fields[k]);
  db.prepare(`UPDATE guild_config SET ${sets} WHERE guild_id = ?`).run(...values, guildId);
}

export function getTickets(guildId: string): Ticket[] {
  return db.prepare("SELECT * FROM tickets WHERE guild_id = ? ORDER BY created_at DESC LIMIT 100").all(guildId) as Ticket[];
}

export function closeTicketRecord(ticketId: number) {
  db.prepare("UPDATE tickets SET status = 'closed', closed_at = ? WHERE id = ?").run(Date.now(), ticketId);
}

export function getLeaderboard(guildId: string, limit = 10): Level[] {
  return db
    .prepare("SELECT * FROM levels WHERE guild_id = ? ORDER BY xp DESC LIMIT ?")
    .all(guildId, limit) as Level[];
}

export function getStats(guildId: string) {
  const totalWarnings = (db.prepare("SELECT COUNT(*) AS c FROM warnings WHERE guild_id = ?").get(guildId) as { c: number }).c;
  const openTickets = (
    db.prepare("SELECT COUNT(*) AS c FROM tickets WHERE guild_id = ? AND status = 'open'").get(guildId) as { c: number }
  ).c;
  const closedTickets = (
    db.prepare("SELECT COUNT(*) AS c FROM tickets WHERE guild_id = ? AND status = 'closed'").get(guildId) as { c: number }
  ).c;
  const trackedMembers = (
    db.prepare("SELECT COUNT(*) AS c FROM levels WHERE guild_id = ?").get(guildId) as { c: number }
  ).c;
  const activeGiveaways = (
    db.prepare("SELECT COUNT(*) AS c FROM giveaways WHERE guild_id = ? AND ended = 0").get(guildId) as { c: number }
  ).c;
  return { totalWarnings, openTickets, closedTickets, trackedMembers, activeGiveaways };
}
