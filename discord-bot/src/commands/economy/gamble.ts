import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { spend, addBalance, getBalance, CURRENCY } from "../../modules/economy.js";
import { errorEmbed, brandEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("gamble")
    .setDescription("Bet your coins on a coinflip — win double or lose it all")
    .addIntegerOption((o) => o.setName("amount").setDescription("How many coins to bet").setRequired(true).setMinValue(10)),

  async execute(interaction: ChatInputCommandInteraction) {
    const amount = interaction.options.getInteger("amount", true);
    const guildId = interaction.guild!.id;

    if (spend(guildId, interaction.user.id, amount) === null) {
      await interaction.reply({ embeds: [errorEmbed("You don't have enough coins for that bet.")], ephemeral: true });
      return;
    }

    const won = Math.random() < 0.5;
    if (won) {
      const balance = addBalance(guildId, interaction.user.id, amount * 2);
      await interaction.reply({
        embeds: [
          brandEmbed()
            .setColor(0x57f287)
            .setTitle("🪙 Heads — You won!")
            .setDescription(`You won **${amount.toLocaleString()}** ${CURRENCY}!\nNew balance: **${balance.toLocaleString()}** ${CURRENCY}`),
        ],
      });
    } else {
      const balance = getBalance(guildId, interaction.user.id);
      await interaction.reply({
        embeds: [
          brandEmbed()
            .setColor(0xed4245)
            .setTitle("🪙 Tails — You lost!")
            .setDescription(`You lost **${amount.toLocaleString()}** ${CURRENCY}.\nNew balance: **${balance.toLocaleString()}** ${CURRENCY}`),
        ],
      });
    }
  },
};

export default command;
