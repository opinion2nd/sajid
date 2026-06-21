import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { claimDaily, formatDuration, CURRENCY } from "../../modules/economy.js";
import { refreshBaltopPanel } from "../../modules/ecoleaderboardpanel.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("daily").setDescription("Claim your daily coin reward"),

  async execute(interaction: ChatInputCommandInteraction) {
    const result = claimDaily(interaction.guild!.id, interaction.user.id);
    if (!result.ok) {
      await interaction.reply({
        embeds: [errorEmbed(`You already claimed your daily reward. Come back in **${formatDuration(result.remainingMs)}**.`)],
        ephemeral: true,
      });
      return;
    }
    await interaction.reply({
      embeds: [successEmbed(`You claimed your daily **${result.amount}** ${CURRENCY}! New balance: **${result.balance.toLocaleString()}**.`)],
    });
    await refreshBaltopPanel(interaction.guild!);
  },
};

export default command;
