import "dotenv/config";
import { Client, Collection, GatewayIntentBits } from "discord.js";
import { loadCommands } from "./handlers/loadCommands.js";
import { loadEvents } from "./handlers/loadEvents.js";

// Flaming License Bot: standalone, self-contained license-key manager — zero
// native dependencies, so it runs the same on a PC, a phone (Termux), or any host.

const token = process.env.DISCORD_BOT_TOKEN;
if (!token) {
  console.error("DISCORD_BOT_TOKEN is not set — bot cannot start.");
  process.exit(1);
}

const client = new Client({
  intents: [GatewayIntentBits.Guilds, GatewayIntentBits.GuildMembers],
});

client.commands = new Collection();

async function main() {
  client.commands = await loadCommands();
  await loadEvents(client);
  await client.login(token);
}

main().catch((error) => {
  console.error("Failed to start bot:", error);
  process.exit(1);
});
