import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed } from "../../util/embeds.js";
import { setAfk } from "../../modules/afk.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("afk")
    .setDescription("Set yourself as AFK")
    .addStringOption((o) => o.setName("reason").setDescription("Why are you AFK?")),

  async execute(interaction: ChatInputCommandInteraction) {
    const reason = interaction.options.getString("reason") ?? "AFK";
    setAfk(interaction.guild!.id, interaction.user.id, reason);
    await interaction.reply({ embeds: [successEmbed(`You're now AFK: ${reason}`)] });
  },
};

export default command;
