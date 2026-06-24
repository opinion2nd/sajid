import { db } from "../db.js";

export interface Product {
  guild_id: string;
  name: string;
  customer_role_id: string | null;
  default_ip_cap: number;
  default_hwid_cap: number;
  created_by: string;
  created_at: number;
}

export interface CreateProductOptions {
  guildId: string;
  name: string;
  customerRoleId?: string | null;
  defaultIpCap?: number;
  defaultHwidCap?: number;
  createdBy: string;
}

export function createProduct(opts: CreateProductOptions): Product {
  db.prepare(
    `INSERT INTO products (guild_id, name, customer_role_id, default_ip_cap, default_hwid_cap, created_by, created_at)
     VALUES (?, ?, ?, ?, ?, ?, ?)`
  ).run(
    opts.guildId,
    opts.name,
    opts.customerRoleId ?? null,
    opts.defaultIpCap ?? 1,
    opts.defaultHwidCap ?? 1,
    opts.createdBy,
    Date.now()
  );
  return getProduct(opts.guildId, opts.name)!;
}

export function getProduct(guildId: string, name: string): Product | undefined {
  return db.prepare("SELECT * FROM products WHERE guild_id = ? AND name = ?").get(guildId, name) as Product | undefined;
}

export function listProducts(guildId: string): Product[] {
  return db.prepare("SELECT * FROM products WHERE guild_id = ? ORDER BY name").all(guildId) as Product[];
}

export interface DeleteProductResult {
  deleted: boolean;
  licensesDeleted: number;
}

export function deleteProduct(guildId: string, name: string, cascadeLicenses: boolean): DeleteProductResult {
  let licensesDeleted = 0;
  if (cascadeLicenses) {
    const licenseKeys = db
      .prepare("SELECT license_key FROM licenses WHERE guild_id = ? AND product_name = ?")
      .all(guildId, name) as { license_key: string }[];
    for (const { license_key } of licenseKeys) {
      db.prepare("DELETE FROM license_ips WHERE license_key = ?").run(license_key);
      db.prepare("DELETE FROM license_hwids WHERE license_key = ?").run(license_key);
    }
    const result = db.prepare("DELETE FROM licenses WHERE guild_id = ? AND product_name = ?").run(guildId, name);
    licensesDeleted = result.changes;
  }
  const deleted = db.prepare("DELETE FROM products WHERE guild_id = ? AND name = ?").run(guildId, name);
  return { deleted: deleted.changes > 0, licensesDeleted };
}
