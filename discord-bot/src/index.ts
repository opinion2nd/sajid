import "dotenv/config";
import { Client, Collection, GatewayIntentBits, Partials } from "discord.js";
import { loadCommands } from "./handlers/loadCommands.js";
import { loadEvents } from "./handlers/loadEvents.js";

// Brother Craft Bot: multi-purpose Discord bot — Moderation, Automod, Tickets,
// Leveling, Giveaways, Welcome/Leave messages, and Reaction-Role panels.

const token = process.env.DISCORD_BOT_TOKEN;
if (!token) {
  console.error("DISCORD_BOT_TOKEN is not set — bot cannot start.");
  process.exit(1);
}

const client = new Client({
  intents: [
    GatewayIntentBits.Guilds,
    GatewayIntentBits.GuildMembers,
    GatewayIntentBits.GuildMessages,
    GatewayIntentBits.MessageContent,
    GatewayIntentBits.GuildModeration,
    GatewayIntentBits.GuildInvites,
  ],
  partials: [Partials.Channel, Partials.Message, Partials.GuildMember],
});

client.commands = new Collection();
client.commandCategories = new Collection();

async function main() {
  const { commands, categories } = await loadCommands();
  client.commands = commands;
  client.commandCategories = categories;
  await loadEvents(client);
  await client.login(token);
}

main().catch((error) => {
  console.error("Failed to start bot:", error);
  process.exit(1);
});

process.on("unhandledRejection", (reason) => {
  console.error("Unhandled promise rejection:", reason);
});

process.on("uncaughtException", (error) => {
  console.error("Uncaught exception:", error);
});
