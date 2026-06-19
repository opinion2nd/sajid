import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { logModAction } from "../../modules/modlog.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("kick")
    .setDescription("Kick a member from the server")
    .setDefaultMemberPermissions(PermissionFlagsBits.KickMembers)
    .addUserOption((o) => o.setName("user").setDescription("Member to kick").setRequired(true))
    .addStringOption((o) => o.setName("reason").setDescription("Reason for the kick")),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getMember("user");
    const reason = interaction.options.getString("reason") ?? "No reason provided";

    if (!target || !("kick" in target)) {
      await interaction.reply({ embeds: [errorEmbed("Could not find that member.")], ephemeral: true });
      return;
    }
    if (!target.kickable) {
      await interaction.reply({
        embeds: [errorEmbed("I can't kick that member (missing permissions or role hierarchy).")],
        ephemeral: true,
      });
      return;
    }

    await target.kick(reason);
    await interaction.reply({ embeds: [successEmbed(`Kicked **${target.user.tag}**. Reason: ${reason}`)] });
    await logModAction(interaction.guild!, {
      action: "Kick",
      target: target.user.tag,
      moderator: interaction.user.tag,
      reason,
    });
  },
};

export default command;
