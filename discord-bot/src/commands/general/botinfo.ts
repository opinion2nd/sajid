import { SlashCommandBuilder, EmbedBuilder, version as djsVersion, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { formatDuration } from "../../util/format.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("botinfo").setDescription("Show information about the bot"),

  async execute(interaction: ChatInputCommandInteraction) {
    const client = interaction.client;
    const embed = new EmbedBuilder()
      .setTitle(`${client.user.username} — Bot Info`)
      .setThumbnail(client.user.displayAvatarURL())
      .setColor(0x5865f2)
      .addFields(
        { name: "Servers", value: String(client.guilds.cache.size), inline: true },
        { name: "Users", value: String(client.guilds.cache.reduce((sum, g) => sum + g.memberCount, 0)), inline: true },
        { name: "Uptime", value: formatDuration(client.uptime ?? 0), inline: true },
        { name: "Discord.js", value: djsVersion, inline: true },
        { name: "Node.js", value: process.version, inline: true },
        { name: "Websocket Ping", value: `${client.ws.ping}ms`, inline: true }
      );
    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
