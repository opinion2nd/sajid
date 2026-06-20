import Database from "better-sqlite3";
import fs from "node:fs";
import path from "node:path";

const dataDir = path.join(process.cwd(), "data");
if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true });

export const db = new Database(path.join(dataDir, "bot.sqlite3"));
db.pragma("journal_mode = WAL");

db.exec(`
  CREATE TABLE IF NOT EXISTS guild_config (
    guild_id TEXT PRIMARY KEY,
    mod_log_channel TEXT,
    welcome_channel TEXT,
    welcome_message TEXT,
    leave_channel TEXT,
    leave_message TEXT,
    levelup_channel TEXT,
    ticket_category TEXT,
    ticket_log_channel TEXT,
    ticket_support_role TEXT,
    automod_enabled INTEGER NOT NULL DEFAULT 0,
    automod_anti_invite INTEGER NOT NULL DEFAULT 1,
    automod_anti_caps INTEGER NOT NULL DEFAULT 1,
    automod_anti_spam INTEGER NOT NULL DEFAULT 1,
    automod_max_mentions INTEGER NOT NULL DEFAULT 5,
    suggestion_channel TEXT,
    verify_role TEXT,
    verify_channel TEXT,
    anti_raid_enabled INTEGER NOT NULL DEFAULT 0,
    raid_join_threshold INTEGER NOT NULL DEFAULT 5,
    raid_window_seconds INTEGER NOT NULL DEFAULT 10,
    raid_account_age_days INTEGER NOT NULL DEFAULT 3,
    anti_nuke_enabled INTEGER NOT NULL DEFAULT 0,
    nuke_threshold INTEGER NOT NULL DEFAULT 3,
    nuke_window_seconds INTEGER NOT NULL DEFAULT 30,
    anti_ghostping_enabled INTEGER NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS warnings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    moderator_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    created_at INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS levels (
    guild_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    xp INTEGER NOT NULL DEFAULT 0,
    level INTEGER NOT NULL DEFAULT 0,
    last_message_at INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (guild_id, user_id)
  );

  CREATE TABLE IF NOT EXISTS tickets (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    channel_id TEXT NOT NULL UNIQUE,
    user_id TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'open',
    created_at INTEGER NOT NULL,
    closed_at INTEGER
  );

  CREATE TABLE IF NOT EXISTS giveaways (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    channel_id TEXT NOT NULL,
    message_id TEXT NOT NULL UNIQUE,
    host_id TEXT NOT NULL,
    prize TEXT NOT NULL,
    winner_count INTEGER NOT NULL DEFAULT 1,
    end_at INTEGER NOT NULL,
    ended INTEGER NOT NULL DEFAULT 0,
    entries TEXT NOT NULL DEFAULT '[]'
  );

  CREATE TABLE IF NOT EXISTS afk (
    guild_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    reason TEXT NOT NULL,
    set_at INTEGER NOT NULL,
    PRIMARY KEY (guild_id, user_id)
  );

  CREATE TABLE IF NOT EXISTS reminders (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id TEXT NOT NULL,
    channel_id TEXT NOT NULL,
    guild_id TEXT NOT NULL,
    remind_at INTEGER NOT NULL,
    message TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    fired INTEGER NOT NULL DEFAULT 0
  );

  CREATE TABLE IF NOT EXISTS suggestions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    channel_id TEXT NOT NULL,
    message_id TEXT NOT NULL UNIQUE,
    user_id TEXT NOT NULL,
    content TEXT NOT NULL,
    upvotes TEXT NOT NULL DEFAULT '[]',
    downvotes TEXT NOT NULL DEFAULT '[]',
    created_at INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS invites (
    guild_id TEXT NOT NULL,
    inviter_id TEXT NOT NULL,
    uses INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (guild_id, inviter_id)
  );

  CREATE TABLE IF NOT EXISTS polls (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    channel_id TEXT NOT NULL,
    message_id TEXT NOT NULL UNIQUE,
    question TEXT NOT NULL,
    options TEXT NOT NULL,
    votes TEXT NOT NULL DEFAULT '{}',
    created_at INTEGER NOT NULL
  );

  CREATE TABLE IF NOT EXISTS backups (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    guild_id TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    data TEXT NOT NULL
  );

  CREATE TABLE IF NOT EXISTS nuke_whitelist (
    guild_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    added_by TEXT NOT NULL,
    added_at INTEGER NOT NULL,
    PRIMARY KEY (guild_id, user_id)
  );

  CREATE TABLE IF NOT EXISTS economy (
    guild_id TEXT NOT NULL,
    user_id TEXT NOT NULL,
    balance INTEGER NOT NULL DEFAULT 0,
    last_daily INTEGER NOT NULL DEFAULT 0,
    last_work INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (guild_id, user_id)
  );
`);

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
