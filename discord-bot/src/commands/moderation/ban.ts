import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { logModAction } from "../../modules/modlog.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("ban")
    .setDescription("Ban a member from the server")
    .setDefaultMemberPermissions(PermissionFlagsBits.BanMembers)
    .addUserOption((o) => o.setName("user").setDescription("Member to ban").setRequired(true))
    .addStringOption((o) => o.setName("reason").setDescription("Reason for the ban"))
    .addIntegerOption((o) =>
      o.setName("delete_days").setDescription("Delete this many days of their messages (0-7)").setMinValue(0).setMaxValue(7)
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const targetUser = interaction.options.getUser("user", true);
    const reason = interaction.options.getString("reason") ?? "No reason provided";
    const deleteDays = interaction.options.getInteger("delete_days") ?? 0;

    const member = await interaction.guild!.members.fetch(targetUser.id).catch(() => null);
    if (member && !member.bannable) {
      await interaction.reply({
        embeds: [errorEmbed("I can't ban that member (missing permissions or role hierarchy).")],
        ephemeral: true,
      });
      return;
    }

    await interaction.guild!.members.ban(targetUser.id, {
      reason,
      deleteMessageSeconds: deleteDays * 86400,
    });

    await interaction.reply({ embeds: [successEmbed(`Banned **${targetUser.tag}**. Reason: ${reason}`)] });
    await logModAction(interaction.guild!, {
      action: "Ban",
      target: targetUser.tag,
      moderator: interaction.user.tag,
      reason,
    });
  },
};

export default command;
