import "dotenv/config";
import { Client, Collection, GatewayIntentBits } from "discord.js";
import { loadCommands } from "./handlers/loadCommands.js";
import { loadEvents } from "./handlers/loadEvents.js";

// License Bot: issues, revokes, and inspects license keys for premium
// Minecraft plugins and Discord bots via the license-server admin API.

const token = process.env.DISCORD_BOT_TOKEN;
if (!token) {
  console.error("DISCORD_BOT_TOKEN is not set — bot cannot start.");
  process.exit(1);
}

const client = new Client({
  intents: [GatewayIntentBits.Guilds],
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
