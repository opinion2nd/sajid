import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { logModAction } from "../../modules/modlog.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("unban")
    .setDescription("Unban a user by their ID")
    .setDefaultMemberPermissions(PermissionFlagsBits.BanMembers)
    .addStringOption((o) => o.setName("user_id").setDescription("The user ID to unban").setRequired(true))
    .addStringOption((o) => o.setName("reason").setDescription("Reason for the unban")),

  async execute(interaction: ChatInputCommandInteraction) {
    const userId = interaction.options.getString("user_id", true).trim();
    const reason = interaction.options.getString("reason") ?? "No reason provided";

    const ban = await interaction.guild!.bans.fetch(userId).catch(() => null);
    if (!ban) {
      await interaction.reply({ embeds: [errorEmbed("That user is not banned.")], ephemeral: true });
      return;
    }

    await interaction.guild!.members.unban(userId, reason);
    await interaction.reply({ embeds: [successEmbed(`Unbanned **${ban.user.tag}**. Reason: ${reason}`)] });
    await logModAction(interaction.guild!, {
      action: "Unban",
      target: ban.user.tag,
      moderator: interaction.user.tag,
      reason,
    });
  },
};

export default command;
