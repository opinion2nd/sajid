import { SlashCommandBuilder } from "discord.js";

// Slash command definitions, shared by the bot runtime and the register script.
export const commands = [
  new SlashCommandBuilder()
    .setName("link")
    .setDescription("Link your Discord account to Brother Craft")
    .addStringOption((o) =>
      o
        .setName("code")
        .setDescription("The link code from your dashboard")
        .setRequired(true)
    ),
  new SlashCommandBuilder()
    .setName("licenses")
    .setDescription("DM yourself your Brother Craft license keys"),
  new SlashCommandBuilder()
    .setName("lookup")
    .setDescription("(Staff) Look up a license key")
    .addStringOption((o) =>
      o.setName("key").setDescription("License key").setRequired(true)
    ),
].map((c) => c.toJSON());
