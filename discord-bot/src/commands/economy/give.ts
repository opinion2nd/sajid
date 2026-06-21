import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { transfer, CURRENCY } from "../../modules/economy.js";
import { refreshBaltopPanel } from "../../modules/ecoleaderboardpanel.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("give")
    .setDescription("Give some of your coins to another member")
    .addUserOption((o) => o.setName("user").setDescription("Recipient").setRequired(true))
    .addIntegerOption((o) => o.setName("amount").setDescription("How many coins").setRequired(true).setMinValue(1)),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user", true);
    const amount = interaction.options.getInteger("amount", true);
    const guildId = interaction.guild!.id;

    if (target.id === interaction.user.id) {
      await interaction.reply({ embeds: [errorEmbed("You can't give coins to yourself.")], ephemeral: true });
      return;
    }
    if (target.bot) {
      await interaction.reply({ embeds: [errorEmbed("You can't give coins to a bot.")], ephemeral: true });
      return;
    }

    const ok = transfer(guildId, interaction.user.id, target.id, amount);
    if (!ok) {
      await interaction.reply({ embeds: [errorEmbed("You don't have enough coins for that.")], ephemeral: true });
      return;
    }
    await interaction.reply({
      embeds: [successEmbed(`You gave ${target} **${amount.toLocaleString()}** ${CURRENCY}.`)],
    });
    await refreshBaltopPanel(interaction.guild!);
  },
};

export default command;
