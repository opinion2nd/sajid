import crypto from "node:crypto";
import type { GuildMember } from "discord.js";
import { getStore, save, type License } from "../db.js";
import { getProduct } from "./products.js";

// Ambiguous characters (I, O, 0, 1) are excluded so keys are easy to read/type.
// 32 symbols divides evenly into 256, so byte-modulo selection below has zero bias.
const CHARSET = process.env.LICENSE_KEY_CHARSET || "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

export interface CreateLicenseOptions {
  guildId: string;
  productName: string;
  discordUserId: string | null;
  createdBy: string;
  expiresAt?: number | null;
  expiresInMsOnRedeem?: number | null;
  ipCap?: number;
  hwidCap?: number;
}

export type AuthFailureReason =
  | "not_found"
  | "unredeemed"
  | "wrong_product"
  | "expired"
  | "ip_cap_reached"
  | "hwid_cap_reached";

export interface AuthResult {
  success: boolean;
  reason?: AuthFailureReason;
  license?: License;
}

export type RedeemFailureReason = "not_found" | "wrong_guild" | "already_redeemed" | "expired";

export interface RedeemResult {
  success: boolean;
  reason?: RedeemFailureReason;
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
  const license: License = {
    licenseKey: generateUniqueLicenseKey(),
    guildId: opts.guildId,
    productName: opts.productName,
    discordUserId: opts.discordUserId,
    createdBy: opts.createdBy,
    createdAt: Date.now(),
    redeemedAt: opts.discordUserId ? Date.now() : null,
    expiresAt: opts.expiresAt ?? null,
    expiresInMsOnRedeem: opts.expiresInMsOnRedeem ?? null,
    ipCap: opts.ipCap ?? 1,
    hwidCap: opts.hwidCap ?? 1,
    totalRequests: 0,
    latestIp: null,
    latestHwid: null,
    latestRequestAt: null,
    ips: [],
    hwids: [],
  };
  getStore().licenses.push(license);
  save();
  return license;
}

export function getLicenseByKey(key: string): License | undefined {
  return getStore().licenses.find((l) => l.licenseKey === key);
}

export function getLicensesByGuild(guildId: string): License[] {
  return getStore()
    .licenses.filter((l) => l.guildId === guildId)
    .sort((a, b) => b.createdAt - a.createdAt);
}

export function getLicensesByUser(guildId: string, discordUserId: string): License[] {
  return getStore()
    .licenses.filter((l) => l.guildId === guildId && l.discordUserId === discordUserId)
    .sort((a, b) => b.createdAt - a.createdAt);
}

export function getLicensesByProduct(guildId: string, productName: string): License[] {
  return getStore()
    .licenses.filter((l) => l.guildId === guildId && l.productName.toLowerCase() === productName.toLowerCase())
    .sort((a, b) => b.createdAt - a.createdAt);
}

export function getUnredeemedStock(guildId: string, productName?: string): License[] {
  return getStore().licenses.filter(
    (l) =>
      l.guildId === guildId &&
      l.discordUserId === null &&
      (!productName || l.productName.toLowerCase() === productName.toLowerCase()),
  );
}

export function deleteLicense(key: string): boolean {
  const store = getStore();
  const index = store.licenses.findIndex((l) => l.licenseKey === key);
  if (index === -1) return false;
  store.licenses.splice(index, 1);
  save();
  return true;
}

export function clearIpList(key: string) {
  const license = getLicenseByKey(key);
  if (!license) return;
  license.ips = [];
  save();
}

export function clearHwidList(key: string) {
  const license = getLicenseByKey(key);
  if (!license) return;
  license.hwids = [];
  save();
}

export function findExpiredLicenses(): License[] {
  const now = Date.now();
  return getStore().licenses.filter((l) => l.expiresAt !== null && l.expiresAt < now);
}

/** True if the user holds another non-expired license whose product grants the same role. */
export function userHasActiveLicenseWithRole(
  guildId: string,
  discordUserId: string,
  roleId: string,
  excludeLicenseKey: string,
): boolean {
  const now = Date.now();
  return getStore().licenses.some((l) => {
    if (l.guildId !== guildId || l.discordUserId !== discordUserId || l.licenseKey === excludeLicenseKey) return false;
    if (l.expiresAt !== null && l.expiresAt < now) return false;
    const product = getProduct(guildId, l.productName);
    return product?.customerRoleId === roleId;
  });
}

/** Claims an unredeemed stock key for a Discord user, starting its redeem-based expiry timer. */
export function redeemLicense(guildId: string, key: string, discordUserId: string): RedeemResult {
  const license = getLicenseByKey(key);
  if (!license) return { success: false, reason: "not_found" };
  if (license.guildId !== guildId) return { success: false, reason: "wrong_guild" };
  if (license.discordUserId !== null) return { success: false, reason: "already_redeemed" };
  if (license.expiresAt !== null && license.expiresAt < Date.now()) return { success: false, reason: "expired" };

  license.discordUserId = discordUserId;
  license.redeemedAt = Date.now();
  if (license.expiresInMsOnRedeem !== null) {
    license.expiresAt = Date.now() + license.expiresInMsOnRedeem;
  }
  save();
  return { success: true, license };
}

export function authenticateLicense(licenseKey: string, productName: string, ip?: string, hwid?: string): AuthResult {
  const license = getLicenseByKey(licenseKey);
  if (!license) return { success: false, reason: "not_found" };
  if (license.discordUserId === null) return { success: false, reason: "unredeemed", license };
  if (license.productName.toLowerCase() !== productName.toLowerCase()) {
    return { success: false, reason: "wrong_product", license };
  }
  if (license.expiresAt && license.expiresAt < Date.now()) {
    return { success: false, reason: "expired", license };
  }

  if (ip) {
    if (!license.ips.includes(ip)) {
      if (license.ips.length >= license.ipCap) return { success: false, reason: "ip_cap_reached", license };
      license.ips.push(ip);
    }
  }

  if (hwid) {
    if (!license.hwids.includes(hwid)) {
      if (license.hwids.length >= license.hwidCap) return { success: false, reason: "hwid_cap_reached", license };
      license.hwids.push(hwid);
    }
  }

  license.totalRequests += 1;
  if (ip) license.latestIp = ip;
  if (hwid) license.latestHwid = hwid;
  license.latestRequestAt = Date.now();
  save();

  return { success: true, license };
}

/** Re-grants customer roles for a rejoining member's still-active licenses. */
export async function restoreLicenseRoles(member: GuildMember) {
  const licenses = getLicensesByUser(member.guild.id, member.id).filter(
    (l) => !l.expiresAt || l.expiresAt > Date.now(),
  );
  for (const license of licenses) {
    const product = getProduct(member.guild.id, license.productName);
    if (product?.customerRoleId && !member.roles.cache.has(product.customerRoleId)) {
      await member.roles.add(product.customerRoleId).catch(() => {});
    }
  }
}
