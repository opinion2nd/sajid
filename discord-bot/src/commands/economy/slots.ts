import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { spend, addBalance, getBalance, CURRENCY } from "../../modules/economy.js";
import { errorEmbed, brandEmbed } from "../../util/embeds.js";

const SYMBOLS = ["🍒", "🍋", "🍇", "🔔", "💎", "7️⃣"];

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("slots")
    .setDescription("Spin the slot machine — three of a kind pays big")
    .addIntegerOption((o) => o.setName("amount").setDescription("How many coins to bet").setRequired(true).setMinValue(10)),

  async execute(interaction: ChatInputCommandInteraction) {
    const amount = interaction.options.getInteger("amount", true);
    const guildId = interaction.guild!.id;

    if (spend(guildId, interaction.user.id, amount) === null) {
      await interaction.reply({ embeds: [errorEmbed("You don't have enough coins for that bet.")], ephemeral: true });
      return;
    }

    const reels = [0, 1, 2].map(() => SYMBOLS[Math.floor(Math.random() * SYMBOLS.length)]);
    const [a, b, c] = reels;

    let multiplier = 0;
    let result = "No match — better luck next time!";
    if (a === b && b === c) {
      multiplier = a === "7️⃣" ? 10 : a === "💎" ? 7 : 4;
      result = `Three of a kind! **${multiplier}×** payout 🎉`;
    } else if (a === b || b === c || a === c) {
      multiplier = 2;
      result = "Two of a kind! **2×** payout 🎈";
    }

    const payout = amount * multiplier;
    if (payout > 0) addBalance(guildId, interaction.user.id, payout);
    const balance = getBalance(guildId, interaction.user.id);

    await interaction.reply({
      embeds: [
        brandEmbed()
          .setColor(multiplier > 0 ? 0x57f287 : 0xed4245)
          .setTitle("🎰 Slots")
          .setDescription(
            `**[ ${reels.join(" | ")} ]**\n\n${result}\n` +
              (payout > 0 ? `You won **${payout.toLocaleString()}** ${CURRENCY}!\n` : `You lost **${amount.toLocaleString()}** ${CURRENCY}.\n`) +
              `Balance: **${balance.toLocaleString()}** ${CURRENCY}`
          ),
      ],
    });
  },
};

export default command;
