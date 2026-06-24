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
      if (license.discordUserId) {
        const product = getProduct(license.guildId, license.productName);
        if (product?.customerRoleId) {
          const guild = client.guilds.cache.get(license.guildId);
          const member = await guild?.members.fetch(license.discordUserId).catch(() => null);
          if (member?.roles.cache.has(product.customerRoleId)) {
            const stillEntitled = userHasActiveLicenseWithRole(
              license.guildId,
              license.discordUserId,
              product.customerRoleId,
              license.licenseKey,
            );
            if (!stillEntitled) await member.roles.remove(product.customerRoleId).catch(() => {});
          }
        }
      }
    } finally {
      deleteLicense(license.licenseKey);
      console.log(`[licenses] Expired license ${license.licenseKey} (${license.productName}) removed.`);
    }
  }
}
