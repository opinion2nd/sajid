import { Events, type Client } from "discord.js";
import { rescheduleActiveGiveaways } from "../modules/giveaways.js";
import { rescheduleActiveReminders } from "../modules/reminders.js";
import { cacheGuildInvites } from "../modules/invites.js";

export const name = Events.ClientReady;
export const once = true;

export async function execute(client: Client<true>) {
  console.log(`Logged in as ${client.user.tag}`);
  rescheduleActiveGiveaways(client);
  rescheduleActiveReminders(client);
  for (const guild of client.guilds.cache.values()) {
    await cacheGuildInvites(guild);
  }
}
