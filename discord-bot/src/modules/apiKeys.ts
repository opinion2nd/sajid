import crypto from "node:crypto";
import { db } from "../db.js";

export const KNOWN_PERMISSIONS = [
  "*",
  "licenses:read",
  "licenses:create",
  "licenses:update",
  "licenses:delete",
  "licenses:*",
  "products:read",
  "products:create",
  "products:update",
  "products:delete",
  "products:*",
  "stats:read",
  "auth",
] as const;

export interface ApiKeyRecord {
  id: number;
  guild_id: string;
  name: string;
  key_hash: string;
  permissions: string; // JSON-encoded string[]
  rate_limit: number;
  created_by: string;
  created_at: number;
}

function hashKey(rawKey: string): string {
  return crypto.createHash("sha256").update(rawKey).digest("hex");
}

function generateRawKey(): string {
  return crypto.randomBytes(32).toString("hex");
}

/** Plaintext is returned exactly once at creation time and never stored — only its hash is kept. */
export function createApiKey(
  guildId: string,
  name: string,
  permissions: string[],
  rateLimit: number,
  createdBy: string
): { record: ApiKeyRecord; plaintextKey: string } {
  const plaintextKey = generateRawKey();
  db.prepare(
    `INSERT INTO api_keys (guild_id, name, key_hash, permissions, rate_limit, created_by, created_at)
     VALUES (?, ?, ?, ?, ?, ?, ?)`
  ).run(guildId, name, hashKey(plaintextKey), JSON.stringify(permissions), rateLimit, createdBy, Date.now());

  const record = db.prepare("SELECT * FROM api_keys WHERE guild_id = ? AND name = ?").get(guildId, name) as ApiKeyRecord;
  return { record, plaintextKey };
}

export function listApiKeys(guildId: string): ApiKeyRecord[] {
  return db.prepare("SELECT * FROM api_keys WHERE guild_id = ? ORDER BY created_at").all(guildId) as ApiKeyRecord[];
}

export function revokeApiKey(guildId: string, name: string): boolean {
  return db.prepare("DELETE FROM api_keys WHERE guild_id = ? AND name = ?").run(guildId, name).changes > 0;
}

export function findApiKeyByRawKey(rawKey: string): ApiKeyRecord | undefined {
  return db.prepare("SELECT * FROM api_keys WHERE key_hash = ?").get(hashKey(rawKey)) as ApiKeyRecord | undefined;
}

export function hasPermission(permissions: string[], required: string): boolean {
  if (permissions.includes("*") || permissions.includes(required)) return true;
  const category = required.split(":")[0];
  return permissions.includes(`${category}:*`);
}
