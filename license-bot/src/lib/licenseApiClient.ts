// Thin HTTP client for the license-server admin API (anti-freecam/license-server).
// Mirrors anti-freecam/keygen-cli's apiCall, extended with a `product` dimension.

const SERVER_URL = (process.env.LICENSE_SERVER_URL ?? "").replace(/\/$/, "");
const ADMIN_SECRET = process.env.LICENSE_ADMIN_SECRET ?? "";

export interface LicenseRow {
  id: string;
  key: string;
  product: string;
  serverId: string | null;
  pluginVersion: string | null;
  createdAt: string;
  expiresAt: string | null;
  revokedAt: string | null;
  lastValidAt: string | null;
  notes: string | null;
}

async function apiCall<T>(method: string, path: string, body?: object): Promise<T> {
  if (!SERVER_URL) throw new Error("LICENSE_SERVER_URL is not set.");
  if (!ADMIN_SECRET) throw new Error("LICENSE_ADMIN_SECRET is not set.");

  const res = await fetch(`${SERVER_URL}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      "X-Admin-Secret": ADMIN_SECRET,
    },
    body: body ? JSON.stringify(body) : undefined,
  });

  const data = (await res.json()) as T;
  if (res.status >= 400) {
    const message = (data as { error?: string })?.error ?? `HTTP ${res.status}`;
    throw new Error(message);
  }
  return data;
}

export async function issueLicense(product: string, notes?: string, expiresAt?: string): Promise<{ key: string }> {
  return apiCall("POST", "/api/v1/admin/generate", { product, notes, expiresAt });
}

export async function revokeLicense(key: string): Promise<void> {
  await apiCall("POST", "/api/v1/admin/revoke", { key });
}

export async function unbindLicense(key: string): Promise<void> {
  await apiCall("POST", "/api/v1/admin/unbind", { key });
}

export async function getLicenses(product?: string): Promise<LicenseRow[]> {
  const query = product ? `?product=${encodeURIComponent(product)}` : "";
  return apiCall("GET", `/api/v1/admin/licenses${query}`);
}

export async function getLicense(key: string): Promise<LicenseRow | undefined> {
  try {
    return await apiCall<LicenseRow>("GET", `/api/v1/admin/licenses/${encodeURIComponent(key)}`);
  } catch {
    return undefined;
  }
}
