import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { getEconomyLeaderboard, CURRENCY } from "../../modules/economy.js";
import { brandEmbed, infoEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("baltop").setDescription("Show the server's richest members"),

  async execute(interaction: ChatInputCommandInteraction) {
    const top = getEconomyLeaderboard(interaction.guild!.id, 10);
    if (top.length === 0) {
      await interaction.reply({ embeds: [infoEmbed("No one has any coins yet.")], ephemeral: true });
      return;
    }

    const medals = ["🥇", "🥈", "🥉"];
    const lines = top.map((row, i) => `${medals[i] ?? `**${i + 1}.**`} <@${row.user_id}> — ${CURRENCY} ${row.balance.toLocaleString()}`);

    const embed = brandEmbed().setColor(0xf1c40f).setTitle("💰 Richest Members").setDescription(lines.join("\n"));
    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
