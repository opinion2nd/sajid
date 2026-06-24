import type { Client } from "discord.js";
import { deleteLicense, findExpiredLicenses, userHasActiveLicenseWithRole } from "./licenses.js";
import { getProduct } from "./products.js";

const SWEEP_INTERVAL_MS = 60_000;

export function startLicenseExpiryWatcher(client: Client) {
  setInterval(() => {
    checkExpiredLicenses(client).catch((error) => console.error("[licenses] Expiry sweep failed:", error));
  }, SWEEP_INTERVAL_MS);
}

async function checkExpiredLicenses(client: Client) {
  for (const license of findExpiredLicenses()) {
    try {
      const product = getProduct(license.guild_id, license.product_name);
      if (product?.customer_role_id) {
        const guild = client.guilds.cache.get(license.guild_id);
        const member = await guild?.members.fetch(license.discord_user_id).catch(() => null);
        if (member?.roles.cache.has(product.customer_role_id)) {
          const stillEntitled = userHasActiveLicenseWithRole(
            license.guild_id,
            license.discord_user_id,
            product.customer_role_id,
            license.license_key
          );
          if (!stillEntitled) await member.roles.remove(product.customer_role_id).catch(() => {});
        }
      }
    } finally {
      deleteLicense(license.license_key);
      console.log(`[licenses] Expired license ${license.license_key} (${license.product_name}) removed.`);
    }
  }
}
