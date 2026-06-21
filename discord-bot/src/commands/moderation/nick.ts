import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("nick")
    .setDescription("Change or reset a member's nickname")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageNicknames)
    .addUserOption((o) => o.setName("user").setDescription("Member to rename").setRequired(true))
    .addStringOption((o) => o.setName("nickname").setDescription("New nickname (leave empty to reset)").setMaxLength(32)),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getMember("user");
    const nickname = interaction.options.getString("nickname");

    if (!target || !("setNickname" in target)) {
      await interaction.reply({ embeds: [errorEmbed("Could not find that member.")], ephemeral: true });
      return;
    }
    if (!target.manageable) {
      await interaction.reply({ embeds: [errorEmbed("I can't change that member's nickname (role hierarchy).")], ephemeral: true });
      return;
    }

    await target.setNickname(nickname ?? null).catch(() => null);
    await interaction.reply({
      embeds: [successEmbed(nickname ? `Set ${target.user}'s nickname to **${nickname}**.` : `Reset ${target.user}'s nickname.`)],
    });
  },
};

export default command;
