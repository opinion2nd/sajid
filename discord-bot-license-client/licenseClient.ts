// Reusable license-check client for Discord bots sold to other server owners.
// Copy this single file into a sold bot's source tree (e.g. src/lib/licenseClient.ts)
// and call requireValidLicense(product) before client.login(token) in index.ts.
//
// Talks to the same license-server (anti-freecam/license-server) used for Minecraft
// plugin licensing — POSTs to {LICENSE_API_URL}/api/v1/validate with the same
// {key, product, serverId, pluginVersion} contract as the Java LicenseValidator.
//
// Required env vars (buyer configures these in their .env):
//   LICENSE_API_URL   e.g. https://license.example.com
//   LICENSE_KEY       the key DM'd to them by the License Bot
//   SERVER_ID         optional — stable install identity; if unset, one is generated
//                      and persisted to ./data/.server-id (set this explicitly on
//                      hosts with an ephemeral filesystem)

import { randomUUID } from "node:crypto";
import { readFileSync, writeFileSync, mkdirSync, existsSync } from "node:fs";
import { dirname } from "node:path";

export type LicenseStatus = "VALID" | "MISMATCH" | "EXPIRED" | "INVALID" | "NETWORK_ERROR";

export interface LicenseCheckResult {
  status: LicenseStatus;
  message: string;
  expiresAt: string | null;
  boundServerId: string | null;
}

const SERVER_ID_FILE = "./data/.server-id";

function getOrCreateServerId(): string {
  const envId = process.env.SERVER_ID;
  if (envId) return envId;

  if (existsSync(SERVER_ID_FILE)) {
    return readFileSync(SERVER_ID_FILE, "utf8").trim();
  }

  const id = randomUUID();
  mkdirSync(dirname(SERVER_ID_FILE), { recursive: true });
  writeFileSync(SERVER_ID_FILE, id, "utf8");
  return id;
}

export async function checkLicense(product: string): Promise<LicenseCheckResult> {
  const apiUrl = (process.env.LICENSE_API_URL ?? "").replace(/\/$/, "");
  const key = process.env.LICENSE_KEY ?? "";

  if (!apiUrl || !key) {
    return {
      status: "NETWORK_ERROR",
      message: "LICENSE_API_URL and LICENSE_KEY must be set.",
      expiresAt: null,
      boundServerId: null,
    };
  }

  const serverId = getOrCreateServerId();

  try {
    const res = await fetch(`${apiUrl}/api/v1/validate`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ key, product, serverId, pluginVersion: "1.0.0" }),
    });
    return (await res.json()) as LicenseCheckResult;
  } catch (err) {
    return {
      status: "NETWORK_ERROR",
      message: `Cannot reach license server: ${(err as Error).message}`,
      expiresAt: null,
      boundServerId: null,
    };
  }
}

export async function requireValidLicense(product: string, opts?: { failOpen?: boolean }): Promise<void> {
  const result = await checkLicense(product);

  if (result.status === "VALID") {
    console.log(`[license] Validated for product "${product}".`);
    return;
  }

  if (result.status === "NETWORK_ERROR" && opts?.failOpen) {
    console.warn(`[license] WARNING: ${result.message} — continuing (failOpen is set).`);
    return;
  }

  console.error(`[license] License check failed (${result.status}): ${result.message}`);
  process.exit(1);
}
