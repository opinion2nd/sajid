import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed, infoEmbed } from "../../util/embeds.js";
import { addWarning, getWarnings, clearWarnings } from "../../modules/moderation.js";
import { logModAction } from "../../modules/modlog.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("warn")
    .setDescription("Warn a member or manage their warnings")
    .setDefaultMemberPermissions(PermissionFlagsBits.ModerateMembers)
    .addSubcommand((sc) =>
      sc
        .setName("add")
        .setDescription("Add a warning to a member")
        .addUserOption((o) => o.setName("user").setDescription("Member to warn").setRequired(true))
        .addStringOption((o) => o.setName("reason").setDescription("Reason for the warning").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("list")
        .setDescription("List a member's warnings")
        .addUserOption((o) => o.setName("user").setDescription("Member to check").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("clear")
        .setDescription("Clear all warnings for a member")
        .addUserOption((o) => o.setName("user").setDescription("Member to clear").setRequired(true))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const targetUser = interaction.options.getUser("user", true);
    const guildId = interaction.guild!.id;

    if (sub === "add") {
      const reason = interaction.options.getString("reason", true);
      addWarning(guildId, targetUser.id, interaction.user.id, reason);
      await interaction.reply({ embeds: [successEmbed(`Warned **${targetUser.tag}**. Reason: ${reason}`)] });
      await logModAction(interaction.guild!, {
        action: "Warn",
        target: targetUser.tag,
        moderator: interaction.user.tag,
        reason,
      });
      return;
    }

    if (sub === "list") {
      const warnings = getWarnings(guildId, targetUser.id);
      if (warnings.length === 0) {
        await interaction.reply({ embeds: [infoEmbed(`**${targetUser.tag}** has no warnings.`)], ephemeral: true });
        return;
      }
      const embed = new EmbedBuilder()
        .setTitle(`Warnings for ${targetUser.tag}`)
        .setColor(0xfee75c)
        .setDescription(
          warnings
            .map((w, i) => `**${i + 1}.** ${w.reason} — <t:${Math.floor(w.created_at / 1000)}:R> by <@${w.moderator_id}>`)
            .join("\n")
        );
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    // clear
    const count = clearWarnings(guildId, targetUser.id);
    await interaction.reply({ embeds: [successEmbed(`Cleared ${count} warning(s) for **${targetUser.tag}**.`)] });
    await logModAction(interaction.guild!, {
      action: "Warnings Cleared",
      target: targetUser.tag,
      moderator: interaction.user.tag,
      reason: `${count} warning(s) removed`,
    });
  },
};

export default command;
