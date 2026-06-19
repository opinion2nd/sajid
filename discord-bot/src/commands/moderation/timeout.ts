import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { logModAction } from "../../modules/modlog.js";
import { parseDuration, formatDuration } from "../../util/format.js";

const MAX_TIMEOUT_MS = 28 * 86_400_000;

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("timeout")
    .setDescription("Timeout or remove a timeout from a member")
    .setDefaultMemberPermissions(PermissionFlagsBits.ModerateMembers)
    .addSubcommand((sc) =>
      sc
        .setName("set")
        .setDescription("Timeout a member")
        .addUserOption((o) => o.setName("user").setDescription("Member to timeout").setRequired(true))
        .addStringOption((o) => o.setName("duration").setDescription("e.g. 10m, 1h, 1d (max 28d)").setRequired(true))
        .addStringOption((o) => o.setName("reason").setDescription("Reason"))
    )
    .addSubcommand((sc) =>
      sc
        .setName("remove")
        .setDescription("Remove a member's timeout")
        .addUserOption((o) => o.setName("user").setDescription("Member to un-timeout").setRequired(true))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const target = interaction.options.getMember("user");

    if (!target || !("timeout" in target)) {
      await interaction.reply({ embeds: [errorEmbed("Could not find that member.")], ephemeral: true });
      return;
    }

    if (sub === "set") {
      const durationStr = interaction.options.getString("duration", true);
      const reason = interaction.options.getString("reason") ?? "No reason provided";
      const ms = parseDuration(durationStr);

      if (!ms || ms > MAX_TIMEOUT_MS) {
        await interaction.reply({
          embeds: [errorEmbed("Invalid duration. Use formats like `10m`, `1h`, `1d` (max 28d).")],
          ephemeral: true,
        });
        return;
      }
      if (!target.moderatable) {
        await interaction.reply({ embeds: [errorEmbed("I can't timeout that member.")], ephemeral: true });
        return;
      }

      await target.timeout(ms, reason);
      await interaction.reply({
        embeds: [successEmbed(`Timed out **${target.user.tag}** for ${formatDuration(ms)}. Reason: ${reason}`)],
      });
      await logModAction(interaction.guild!, {
        action: "Timeout",
        target: target.user.tag,
        moderator: interaction.user.tag,
        reason,
        extra: formatDuration(ms),
      });
      return;
    }

    if (!target.moderatable) {
      await interaction.reply({ embeds: [errorEmbed("I can't remove that member's timeout.")], ephemeral: true });
      return;
    }

    await target.timeout(null);
    await interaction.reply({ embeds: [successEmbed(`Removed timeout for **${target.user.tag}**.`)] });
    await logModAction(interaction.guild!, {
      action: "Timeout Removed",
      target: target.user.tag,
      moderator: interaction.user.tag,
      reason: "—",
    });
  },
};

export default command;
