import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("coinflip").setDescription("Flip a coin"),

  async execute(interaction: ChatInputCommandInteraction) {
    const result = Math.random() < 0.5 ? "Heads" : "Tails";
    await interaction.reply({ embeds: [infoEmbed(`🪙 The coin landed on **${result}**!`)] });
  },
};

export default command;
