import "dotenv/config";
import { REST, Routes } from "discord.js";
import { loadCommands } from "./handlers/loadCommands.js";

// Registers slash commands with Discord. Run after changing commands:
//   pnpm deploy-commands
const token = process.env.DISCORD_BOT_TOKEN;
const clientId = process.env.DISCORD_CLIENT_ID;
const guildId = process.env.DISCORD_GUILD_ID;

if (!token || !clientId) {
  console.error("DISCORD_BOT_TOKEN and DISCORD_CLIENT_ID are required.");
  process.exit(1);
}

const { commands } = await loadCommands();
const body = commands.map((c) => c.data.toJSON());

const rest = new REST({ version: "10" }).setToken(token);
const route = guildId ? Routes.applicationGuildCommands(clientId, guildId) : Routes.applicationCommands(clientId);

await rest.put(route, { body });
console.log(`Registered ${body.length} commands ${guildId ? `to guild ${guildId}` : "globally"}.`);
