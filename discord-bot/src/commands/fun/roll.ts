import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("roll")
    .setDescription("Roll dice")
    .addStringOption((o) => o.setName("dice").setDescription("Format: NdM, e.g. 2d6 (default 1d6)")),

  async execute(interaction: ChatInputCommandInteraction) {
    const input = interaction.options.getString("dice") ?? "1d6";
    const match = input.trim().match(/^(\d+)d(\d+)$/i);
    if (!match) {
      await interaction.reply({ embeds: [errorEmbed("Invalid format. Use `NdM`, e.g. `2d6`.")], ephemeral: true });
      return;
    }

    const count = Math.min(Number(match[1]), 20);
    const sides = Math.min(Number(match[2]), 1000);
    if (count < 1 || sides < 1) {
      await interaction.reply({ embeds: [errorEmbed("Dice count and sides must be at least 1.")], ephemeral: true });
      return;
    }

    const rolls = Array.from({ length: count }, () => Math.floor(Math.random() * sides) + 1);
    const total = rolls.reduce((a, b) => a + b, 0);
    await interaction.reply({ embeds: [infoEmbed(`🎲 Rolled **${input}**: [${rolls.join(", ")}] — Total: **${total}**`)] });
  },
};

export default command;
