import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { doWork, formatDuration, CURRENCY } from "../../modules/economy.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("work").setDescription("Work for some coins (hourly)"),

  async execute(interaction: ChatInputCommandInteraction) {
    const result = doWork(interaction.guild!.id, interaction.user.id);
    if (!result.ok) {
      await interaction.reply({
        embeds: [errorEmbed(`You're tired. You can work again in **${formatDuration(result.remainingMs)}**.`)],
        ephemeral: true,
      });
      return;
    }
    await interaction.reply({
      embeds: [successEmbed(`${result.line} **${result.amount}** ${CURRENCY}! New balance: **${result.balance.toLocaleString()}**.`)],
    });
  },
};

export default command;
