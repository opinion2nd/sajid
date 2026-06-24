import { Events, type Client } from "discord.js";
import { startLicenseExpiryWatcher } from "../modules/licenseExpiry.js";
import { startApiServer } from "../api/server.js";

export const name = Events.ClientReady;
export const once = true;

export async function execute(client: Client<true>) {
  console.log(`Logged in as ${client.user.tag}`);
  startLicenseExpiryWatcher(client);
  startApiServer();
}
