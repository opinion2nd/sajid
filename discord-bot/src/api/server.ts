import http from "node:http";
import { findApiKeyByRawKey, hasPermission } from "../modules/apiKeys.js";
import * as Licenses from "../modules/licenses.js";
import * as Products from "../modules/products.js";

const rateLimitWindows = new Map<number, { count: number; resetAt: number }>();

function checkRateLimit(apiKeyId: number, limit: number): boolean {
  const now = Date.now();
  const entry = rateLimitWindows.get(apiKeyId);
  if (!entry || entry.resetAt < now) {
    rateLimitWindows.set(apiKeyId, { count: 1, resetAt: now + 60_000 });
    return true;
  }
  if (entry.count >= limit) return false;
  entry.count++;
  return true;
}

function sendJson(res: http.ServerResponse, status: number, body: unknown) {
  const data = JSON.stringify(body);
  res.writeHead(status, { "Content-Type": "application/json", "Content-Length": Buffer.byteLength(data) });
  res.end(data);
}

async function readJsonBody(req: http.IncomingMessage): Promise<Record<string, any>> {
  const chunks: Buffer[] = [];
  for await (const chunk of req as AsyncIterable<Buffer>) chunks.push(chunk);
  if (chunks.length === 0) return {};
  try {
    return JSON.parse(Buffer.concat(chunks).toString("utf8"));
  } catch {
    return {};
  }
}

function serializeLicense(license: Licenses.License) {
  return {
    licenseKey: license.license_key,
    productName: license.product_name,
    discordUserId: license.discord_user_id,
    builtbybitUserId: license.builtbybit_user_id,
    createdAt: license.created_at,
    expiresAt: license.expires_at,
    ipCap: license.ip_cap,
    hwidCap: license.hwid_cap,
    totalRequests: license.total_requests,
  };
}

export function startApiServer(): http.Server {
  const port = Number(process.env.LICENSE_API_PORT ?? 3000);
  const server = http.createServer((req, res) => {
    handleRequest(req, res).catch((error) => {
      console.error("[api] Unhandled error:", error);
      if (!res.headersSent) sendJson(res, 500, { error: "internal_server_error" });
    });
  });
  server.listen(port, () => console.log(`[api] License verification API listening on port ${port}`));
  return server;
}

async function handleRequest(req: http.IncomingMessage, res: http.ServerResponse) {
  const url = new URL(req.url ?? "/", "http://localhost");
  const segments = url.pathname.split("/").filter(Boolean);

  if (segments[0] !== "api") return sendJson(res, 404, { error: "not_found" });

  const authHeader = req.headers.authorization;
  const rawHeader = Array.isArray(authHeader) ? authHeader[0] : authHeader;
  const rawKey = rawHeader?.startsWith("Bearer ") ? rawHeader.slice(7) : rawHeader;
  if (!rawKey) return sendJson(res, 401, { error: "missing_api_key" });

  const apiKey = findApiKeyByRawKey(rawKey);
  if (!apiKey) return sendJson(res, 401, { error: "invalid_api_key" });
  if (!checkRateLimit(apiKey.id, apiKey.rate_limit)) return sendJson(res, 429, { error: "rate_limited" });

  const permissions = JSON.parse(apiKey.permissions) as string[];
  const guildId = apiKey.guild_id;
  const allow = (permission: string) => hasPermission(permissions, permission);

  if (segments[1] === "auth" && segments[2] === "verify" && req.method === "POST") {
    if (!allow("auth")) return sendJson(res, 403, { error: "forbidden" });
    const body = await readJsonBody(req);
    if (!body.licenseKey || !body.product) return sendJson(res, 400, { error: "licenseKey and product are required" });

    const result = Licenses.authenticateLicense(body.licenseKey, body.product, body.ip, body.hwid);
    if (result.license && result.license.guild_id !== guildId) return sendJson(res, 200, { valid: false, reason: "not_found" });
    if (!result.success) return sendJson(res, 200, { valid: false, reason: result.reason });
    return sendJson(res, 200, { valid: true, license: serializeLicense(result.license!) });
  }

  if (segments[1] === "licenses") {
    if (segments.length === 2 && req.method === "GET") {
      if (!allow("licenses:read")) return sendJson(res, 403, { error: "forbidden" });
      let licenses = Licenses.getLicensesByGuild(guildId);
      const product = url.searchParams.get("product");
      const discordUserId = url.searchParams.get("discordUserId");
      if (product) licenses = licenses.filter((l) => l.product_name.toLowerCase() === product.toLowerCase());
      if (discordUserId) licenses = licenses.filter((l) => l.discord_user_id === discordUserId);
      return sendJson(res, 200, { licenses: licenses.map(serializeLicense) });
    }

    if (segments.length === 2 && req.method === "POST") {
      if (!allow("licenses:create")) return sendJson(res, 403, { error: "forbidden" });
      const body = await readJsonBody(req);
      if (!body.productName || !body.discordUserId) {
        return sendJson(res, 400, { error: "productName and discordUserId are required" });
      }
      const product = Products.getProduct(guildId, body.productName);
      if (!product) return sendJson(res, 404, { error: "product_not_found" });
      const license = Licenses.createLicense({
        guildId,
        productName: body.productName,
        discordUserId: body.discordUserId,
        builtbybitUserId: body.builtbybitUserId ?? null,
        createdBy: `api:${apiKey.name}`,
        expiresAt: body.expiresInDays ? Date.now() + Number(body.expiresInDays) * 86_400_000 : null,
        ipCap: body.ipCap ?? product.default_ip_cap,
        hwidCap: body.hwidCap ?? product.default_hwid_cap,
      });
      return sendJson(res, 201, { license: serializeLicense(license) });
    }

    if (segments.length === 3 && req.method === "GET") {
      if (!allow("licenses:read")) return sendJson(res, 403, { error: "forbidden" });
      const license = Licenses.getLicenseByKey(segments[2]);
      if (!license || license.guild_id !== guildId) return sendJson(res, 404, { error: "not_found" });
      return sendJson(res, 200, { license: serializeLicense(license) });
    }

    if (segments.length === 3 && req.method === "DELETE") {
      if (!allow("licenses:delete")) return sendJson(res, 403, { error: "forbidden" });
      const license = Licenses.getLicenseByKey(segments[2]);
      if (!license || license.guild_id !== guildId) return sendJson(res, 404, { error: "not_found" });
      Licenses.deleteLicense(segments[2]);
      return sendJson(res, 200, { deleted: true });
    }

    if (segments.length === 4 && segments[3] === "clear-ip" && req.method === "POST") {
      if (!allow("licenses:update")) return sendJson(res, 403, { error: "forbidden" });
      const license = Licenses.getLicenseByKey(segments[2]);
      if (!license || license.guild_id !== guildId) return sendJson(res, 404, { error: "not_found" });
      Licenses.clearIpList(segments[2]);
      return sendJson(res, 200, { cleared: true });
    }

    if (segments.length === 4 && segments[3] === "clear-hwid" && req.method === "POST") {
      if (!allow("licenses:update")) return sendJson(res, 403, { error: "forbidden" });
      const license = Licenses.getLicenseByKey(segments[2]);
      if (!license || license.guild_id !== guildId) return sendJson(res, 404, { error: "not_found" });
      Licenses.clearHwidList(segments[2]);
      return sendJson(res, 200, { cleared: true });
    }
  }

  if (segments[1] === "products") {
    if (segments.length === 2 && req.method === "GET") {
      if (!allow("products:read")) return sendJson(res, 403, { error: "forbidden" });
      return sendJson(res, 200, { products: Products.listProducts(guildId) });
    }

    if (segments.length === 2 && req.method === "POST") {
      if (!allow("products:create")) return sendJson(res, 403, { error: "forbidden" });
      const body = await readJsonBody(req);
      if (!body.name) return sendJson(res, 400, { error: "name is required" });
      if (Products.getProduct(guildId, body.name)) return sendJson(res, 409, { error: "already_exists" });
      const product = Products.createProduct({
        guildId,
        name: body.name,
        customerRoleId: body.customerRoleId ?? null,
        defaultIpCap: body.defaultIpCap,
        defaultHwidCap: body.defaultHwidCap,
        createdBy: `api:${apiKey.name}`,
      });
      return sendJson(res, 201, { product });
    }

    if (segments.length === 3 && req.method === "DELETE") {
      if (!allow("products:delete")) return sendJson(res, 403, { error: "forbidden" });
      const cascade = url.searchParams.get("cascade") === "true";
      const result = Products.deleteProduct(guildId, segments[2], cascade);
      if (!result.deleted) return sendJson(res, 404, { error: "not_found" });
      return sendJson(res, 200, result);
    }
  }

  if (segments[1] === "stats" && segments.length === 2 && req.method === "GET") {
    if (!allow("stats:read")) return sendJson(res, 403, { error: "forbidden" });
    const licenses = Licenses.getLicensesByGuild(guildId);
    const products = Products.listProducts(guildId);
    return sendJson(res, 200, {
      totalProducts: products.length,
      totalLicenses: licenses.length,
      totalRequests: licenses.reduce((sum, l) => sum + l.total_requests, 0),
    });
  }

  return sendJson(res, 404, { error: "not_found" });
}
