import crypto from "node:crypto";
import { getStore, save, type ApiKeyRecord } from "../db.js";

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
  createdBy: string,
): { record: ApiKeyRecord; plaintextKey: string } {
  const plaintextKey = generateRawKey();
  const record: ApiKeyRecord = {
    guildId,
    name,
    keyHash: hashKey(plaintextKey),
    permissions,
    rateLimit,
    createdBy,
    createdAt: Date.now(),
  };
  getStore().apiKeys.push(record);
  save();
  return { record, plaintextKey };
}

export function listApiKeys(guildId: string): ApiKeyRecord[] {
  return getStore()
    .apiKeys.filter((k) => k.guildId === guildId)
    .sort((a, b) => a.createdAt - b.createdAt);
}

export function revokeApiKey(guildId: string, name: string): boolean {
  const store = getStore();
  const index = store.apiKeys.findIndex((k) => k.guildId === guildId && k.name === name);
  if (index === -1) return false;
  store.apiKeys.splice(index, 1);
  save();
  return true;
}

export function findApiKeyByRawKey(rawKey: string): ApiKeyRecord | undefined {
  const hash = hashKey(rawKey);
  return getStore().apiKeys.find((k) => k.keyHash === hash);
}

export function hasPermission(permissions: string[], required: string): boolean {
  if (permissions.includes("*") || permissions.includes(required)) return true;
  const category = required.split(":")[0];
  return permissions.includes(`${category}:*`);
}
