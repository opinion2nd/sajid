import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed } from "../../util/embeds.js";
import { getInviteCount } from "../../modules/invites.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("invites")
    .setDescription("Show how many members someone has invited")
    .addUserOption((o) => o.setName("user").setDescription("The user to look up")),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user") ?? interaction.user;
    const count = getInviteCount(interaction.guild!.id, target.id);
    await interaction.reply({ embeds: [infoEmbed(`**${target.tag}** has invited **${count}** member(s).`)] });
  },
};

export default command;
