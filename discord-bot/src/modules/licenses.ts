import crypto from "node:crypto";
import type { GuildMember } from "discord.js";
import { db } from "../db.js";
import { getProduct } from "./products.js";

// Ambiguous characters (I, O, 0, 1) are excluded so keys are easy to read/type.
// 32 symbols divides evenly into 256, so byte-modulo selection below has zero bias.
const CHARSET = process.env.LICENSE_KEY_CHARSET || "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

export interface License {
  id: number;
  guild_id: string;
  license_key: string;
  product_name: string;
  discord_user_id: string;
  builtbybit_user_id: string | null;
  created_by: string;
  created_at: number;
  expires_at: number | null;
  ip_cap: number;
  hwid_cap: number;
  total_requests: number;
  latest_ip: string | null;
  latest_hwid: string | null;
  latest_request_at: number | null;
}

export interface CreateLicenseOptions {
  guildId: string;
  productName: string;
  discordUserId: string;
  builtbybitUserId?: string | null;
  createdBy: string;
  expiresAt?: number | null;
  ipCap?: number;
  hwidCap?: number;
}

export type AuthFailureReason = "not_found" | "wrong_product" | "expired" | "ip_cap_reached" | "hwid_cap_reached";

export interface AuthResult {
  success: boolean;
  reason?: AuthFailureReason;
  license?: License;
}

function randomKey(length = 16): string {
  const bytes = crypto.randomBytes(length);
  let out = "";
  for (let i = 0; i < length; i++) out += CHARSET[bytes[i] % CHARSET.length];
  return out.match(/.{1,4}/g)!.join("-");
}

export function generateUniqueLicenseKey(): string {
  let key = randomKey();
  while (getLicenseByKey(key)) key = randomKey();
  return key;
}

export function createLicense(opts: CreateLicenseOptions): License {
  const key = generateUniqueLicenseKey();
  db.prepare(
    `INSERT INTO licenses (guild_id, license_key, product_name, discord_user_id, builtbybit_user_id, created_by, created_at, expires_at, ip_cap, hwid_cap)
     VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`
  ).run(
    opts.guildId,
    key,
    opts.productName,
    opts.discordUserId,
    opts.builtbybitUserId ?? null,
    opts.createdBy,
    Date.now(),
    opts.expiresAt ?? null,
    opts.ipCap ?? 1,
    opts.hwidCap ?? 1
  );
  return getLicenseByKey(key)!;
}

export function getLicenseByKey(key: string): License | undefined {
  return db.prepare("SELECT * FROM licenses WHERE license_key = ?").get(key) as License | undefined;
}

export function getLicensesByGuild(guildId: string): License[] {
  return db.prepare("SELECT * FROM licenses WHERE guild_id = ? ORDER BY created_at DESC").all(guildId) as License[];
}

export function getLicensesByUser(guildId: string, discordUserId: string): License[] {
  return db
    .prepare("SELECT * FROM licenses WHERE guild_id = ? AND discord_user_id = ? ORDER BY created_at DESC")
    .all(guildId, discordUserId) as License[];
}

export function getLicensesByProduct(guildId: string, productName: string): License[] {
  return db
    .prepare("SELECT * FROM licenses WHERE guild_id = ? AND product_name = ? ORDER BY created_at DESC")
    .all(guildId, productName) as License[];
}

export function deleteLicense(key: string): boolean {
  const result = db.prepare("DELETE FROM licenses WHERE license_key = ?").run(key);
  db.prepare("DELETE FROM license_ips WHERE license_key = ?").run(key);
  db.prepare("DELETE FROM license_hwids WHERE license_key = ?").run(key);
  return result.changes > 0;
}

export function getIpList(key: string): { ip: string; created_at: number }[] {
  return db.prepare("SELECT ip, created_at FROM license_ips WHERE license_key = ? ORDER BY created_at").all(key) as {
    ip: string;
    created_at: number;
  }[];
}

export function getHwidList(key: string): { hwid: string; created_at: number }[] {
  return db
    .prepare("SELECT hwid, created_at FROM license_hwids WHERE license_key = ? ORDER BY created_at")
    .all(key) as { hwid: string; created_at: number }[];
}

export function clearIpList(key: string) {
  db.prepare("DELETE FROM license_ips WHERE license_key = ?").run(key);
}

export function clearHwidList(key: string) {
  db.prepare("DELETE FROM license_hwids WHERE license_key = ?").run(key);
}

export function findExpiredLicenses(): License[] {
  return db.prepare("SELECT * FROM licenses WHERE expires_at IS NOT NULL AND expires_at < ?").all(Date.now()) as License[];
}

/** True if the user holds another non-expired license whose product grants the same role. */
export function userHasActiveLicenseWithRole(
  guildId: string,
  discordUserId: string,
  roleId: string,
  excludeLicenseKey: string
): boolean {
  const rows = db
    .prepare(
      `SELECT l.license_key FROM licenses l
       JOIN products p ON p.guild_id = l.guild_id AND p.name = l.product_name
       WHERE l.guild_id = ? AND l.discord_user_id = ? AND p.customer_role_id = ?
         AND (l.expires_at IS NULL OR l.expires_at > ?) AND l.license_key != ?`
    )
    .all(guildId, discordUserId, roleId, Date.now(), excludeLicenseKey);
  return rows.length > 0;
}

export function authenticateLicense(licenseKey: string, productName: string, ip?: string, hwid?: string): AuthResult {
  const license = getLicenseByKey(licenseKey);
  if (!license) return { success: false, reason: "not_found" };
  if (license.product_name.toLowerCase() !== productName.toLowerCase()) {
    return { success: false, reason: "wrong_product", license };
  }
  if (license.expires_at && license.expires_at < Date.now()) {
    return { success: false, reason: "expired", license };
  }

  if (ip) {
    const ips = getIpList(license.license_key);
    if (!ips.some((r) => r.ip === ip)) {
      if (ips.length >= license.ip_cap) return { success: false, reason: "ip_cap_reached", license };
      db.prepare("INSERT OR IGNORE INTO license_ips (license_key, ip, created_at) VALUES (?, ?, ?)").run(
        license.license_key,
        ip,
        Date.now()
      );
    }
  }

  if (hwid) {
    const hwids = getHwidList(license.license_key);
    if (!hwids.some((r) => r.hwid === hwid)) {
      if (hwids.length >= license.hwid_cap) return { success: false, reason: "hwid_cap_reached", license };
      db.prepare("INSERT OR IGNORE INTO license_hwids (license_key, hwid, created_at) VALUES (?, ?, ?)").run(
        license.license_key,
        hwid,
        Date.now()
      );
    }
  }

  db.prepare(
    `UPDATE licenses SET total_requests = total_requests + 1,
       latest_ip = COALESCE(?, latest_ip), latest_hwid = COALESCE(?, latest_hwid), latest_request_at = ?
     WHERE license_key = ?`
  ).run(ip ?? null, hwid ?? null, Date.now(), license.license_key);

  return { success: true, license: getLicenseByKey(license.license_key) };
}

/** Re-grants customer roles for a rejoining member's still-active licenses. */
export async function restoreLicenseRoles(member: GuildMember) {
  const licenses = getLicensesByUser(member.guild.id, member.id).filter(
    (l) => !l.expires_at || l.expires_at > Date.now()
  );
  for (const license of licenses) {
    const product = getProduct(member.guild.id, license.product_name);
    if (product?.customer_role_id && !member.roles.cache.has(product.customer_role_id)) {
      await member.roles.add(product.customer_role_id).catch(() => {});
    }
  }
}
