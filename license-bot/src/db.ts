import fs from "node:fs";
import path from "node:path";

const dataDir = path.join(process.cwd(), "data");
if (!fs.existsSync(dataDir)) fs.mkdirSync(dataDir, { recursive: true });
const dataFile = path.join(dataDir, "license-bot.json");

export interface Product {
  guildId: string;
  name: string;
  customerRoleId: string | null;
  defaultIpCap: number;
  defaultHwidCap: number;
  createdBy: string;
  createdAt: number;
}

export interface License {
  licenseKey: string;
  guildId: string;
  productName: string;
  discordUserId: string | null;
  createdBy: string;
  createdAt: number;
  redeemedAt: number | null;
  expiresAt: number | null;
  expiresInMsOnRedeem: number | null;
  ipCap: number;
  hwidCap: number;
  totalRequests: number;
  latestIp: string | null;
  latestHwid: string | null;
  latestRequestAt: number | null;
  ips: string[];
  hwids: string[];
}

export interface ApiKeyRecord {
  guildId: string;
  name: string;
  keyHash: string;
  permissions: string[];
  rateLimit: number;
  createdBy: string;
  createdAt: number;
}

export interface GuildSettings {
  webhookUrl?: string;
}

interface Store {
  products: Product[];
  licenses: License[];
  apiKeys: ApiKeyRecord[];
  guildSettings: Record<string, GuildSettings>;
}

function emptyStore(): Store {
  return { products: [], licenses: [], apiKeys: [], guildSettings: {} };
}

function loadStore(): Store {
  if (!fs.existsSync(dataFile)) return emptyStore();
  try {
    const parsed = JSON.parse(fs.readFileSync(dataFile, "utf8"));
    return { ...emptyStore(), ...parsed };
  } catch (error) {
    console.error("[db] Failed to read data file, starting fresh:", error);
    return emptyStore();
  }
}

const store: Store = loadStore();

export function save() {
  const tmpFile = `${dataFile}.tmp`;
  fs.writeFileSync(tmpFile, JSON.stringify(store, null, 2));
  fs.renameSync(tmpFile, dataFile);
}

export function getStore(): Store {
  return store;
}
