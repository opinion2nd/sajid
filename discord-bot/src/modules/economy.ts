import { db } from "../db.js";

export const CURRENCY = "🪙";
export const DAILY_AMOUNT = 250;
export const DAILY_COOLDOWN_MS = 24 * 60 * 60 * 1000;
export const WORK_COOLDOWN_MS = 60 * 60 * 1000;
export const WORK_MIN = 50;
export const WORK_MAX = 200;

const WORK_LINES = [
  "You fixed a server bug and earned",
  "You streamed for a few hours and tipped",
  "You delivered pizzas around the city and made",
  "You sold some homemade memes and got",
  "You helped a neighbour move and were paid",
  "You walked dogs all afternoon and earned",
];

interface EconRow {
  balance: number;
  last_daily: number;
  last_work: number;
}

function getRow(guildId: string, userId: string): EconRow {
  let row = db
    .prepare("SELECT balance, last_daily, last_work FROM economy WHERE guild_id = ? AND user_id = ?")
    .get(guildId, userId) as EconRow | undefined;
  if (!row) {
    db.prepare("INSERT INTO economy (guild_id, user_id) VALUES (?, ?)").run(guildId, userId);
    row = { balance: 0, last_daily: 0, last_work: 0 };
  }
  return row;
}

export function getBalance(guildId: string, userId: string): number {
  return getRow(guildId, userId).balance;
}

export function addBalance(guildId: string, userId: string, amount: number): number {
  const row = getRow(guildId, userId);
  const next = row.balance + amount;
  db.prepare("UPDATE economy SET balance = ? WHERE guild_id = ? AND user_id = ?").run(next, guildId, userId);
  return next;
}

/** Attempts to claim the daily reward. Returns the amount, or the ms remaining until it's ready. */
export function claimDaily(guildId: string, userId: string): { ok: true; amount: number; balance: number } | { ok: false; remainingMs: number } {
  const row = getRow(guildId, userId);
  const now = Date.now();
  const elapsed = now - row.last_daily;
  if (elapsed < DAILY_COOLDOWN_MS) return { ok: false, remainingMs: DAILY_COOLDOWN_MS - elapsed };
  const balance = row.balance + DAILY_AMOUNT;
  db.prepare("UPDATE economy SET balance = ?, last_daily = ? WHERE guild_id = ? AND user_id = ?").run(balance, now, guildId, userId);
  return { ok: true, amount: DAILY_AMOUNT, balance };
}

/** Attempts to work. Returns a flavour line + amount, or the ms remaining until it's ready. */
export function doWork(guildId: string, userId: string): { ok: true; amount: number; balance: number; line: string } | { ok: false; remainingMs: number } {
  const row = getRow(guildId, userId);
  const now = Date.now();
  const elapsed = now - row.last_work;
  if (elapsed < WORK_COOLDOWN_MS) return { ok: false, remainingMs: WORK_COOLDOWN_MS - elapsed };
  const amount = WORK_MIN + Math.floor(Math.random() * (WORK_MAX - WORK_MIN + 1));
  const balance = row.balance + amount;
  const line = WORK_LINES[Math.floor(Math.random() * WORK_LINES.length)];
  db.prepare("UPDATE economy SET balance = ?, last_work = ? WHERE guild_id = ? AND user_id = ?").run(balance, now, guildId, userId);
  return { ok: true, amount, balance, line };
}

/** Transfers coins between two users. Returns false if the sender lacks funds. */
export function transfer(guildId: string, fromId: string, toId: string, amount: number): boolean {
  const from = getRow(guildId, fromId);
  if (from.balance < amount) return false;
  addBalance(guildId, fromId, -amount);
  addBalance(guildId, toId, amount);
  return true;
}

export function getEconomyLeaderboard(guildId: string, limit = 10) {
  return db
    .prepare("SELECT user_id, balance FROM economy WHERE guild_id = ? ORDER BY balance DESC LIMIT ?")
    .all(guildId, limit) as { user_id: string; balance: number }[];
}

/** Formats a ms duration as a short "2h 5m" style string. */
export function formatDuration(ms: number): string {
  const totalMinutes = Math.ceil(ms / 60000);
  const hours = Math.floor(totalMinutes / 60);
  const minutes = totalMinutes % 60;
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
}
