import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { getBalance, CURRENCY } from "../../modules/economy.js";
import { brandEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("balance")
    .setDescription("Check your or another member's coin balance")
    .addUserOption((o) => o.setName("user").setDescription("Member to check")),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user") ?? interaction.user;
    const balance = getBalance(interaction.guild!.id, target.id);
    const embed = brandEmbed()
      .setAuthor({ name: `${target.username}'s wallet`, iconURL: target.displayAvatarURL() })
      .setDescription(`${CURRENCY} **${balance.toLocaleString()}** coins`);
    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
