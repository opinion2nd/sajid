import { REST, Routes } from "discord.js";
import { commands } from "./commands";

// Registers slash commands with Discord. Run once after changing commands:
//   pnpm --filter @brothercraft/bot register
const token = process.env.DISCORD_BOT_TOKEN;
const clientId = process.env.DISCORD_CLIENT_ID;
const guildId = process.env.DISCORD_GUILD_ID;

if (!token || !clientId) {
  console.error("DISCORD_BOT_TOKEN and DISCORD_CLIENT_ID are required.");
  process.exit(1);
}

const rest = new REST({ version: "10" }).setToken(token);

const route = guildId
  ? Routes.applicationGuildCommands(clientId, guildId)
  : Routes.applicationCommands(clientId);

await rest.put(route, { body: commands });
console.log(
  `Registered ${commands.length} commands ${guildId ? `to guild ${guildId}` : "globally"}.`
);
