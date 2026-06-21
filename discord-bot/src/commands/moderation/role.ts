import { SlashCommandBuilder, PermissionFlagsBits, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { logModAction } from "../../modules/modlog.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("role")
    .setDescription("Add or remove a role from a member")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageRoles)
    .addSubcommand((sc) =>
      sc
        .setName("add")
        .setDescription("Give a member a role")
        .addUserOption((o) => o.setName("user").setDescription("Member").setRequired(true))
        .addRoleOption((o) => o.setName("role").setDescription("Role to add").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("remove")
        .setDescription("Take a role from a member")
        .addUserOption((o) => o.setName("user").setDescription("Member").setRequired(true))
        .addRoleOption((o) => o.setName("role").setDescription("Role to remove").setRequired(true))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const user = interaction.options.getUser("user", true);
    const role = interaction.options.getRole("role", true);

    const target = await interaction.guild!.members.fetch(user.id).catch(() => null);
    if (!target) {
      await interaction.reply({ embeds: [errorEmbed("Could not find that member.")], ephemeral: true });
      return;
    }

    const me = interaction.guild!.members.me;
    if (me && role.position >= me.roles.highest.position) {
      await interaction.reply({ embeds: [errorEmbed("That role is higher than mine — I can't manage it (check role hierarchy).")], ephemeral: true });
      return;
    }

    try {
      if (sub === "add") {
        await target.roles.add(role.id);
        await interaction.reply({ embeds: [successEmbed(`Added **${role.name}** to ${target.user}.`)] });
        await logModAction(interaction.guild!, { action: "Role Add", target: target.user.tag, moderator: interaction.user.tag, reason: role.name });
      } else {
        await target.roles.remove(role.id);
        await interaction.reply({ embeds: [successEmbed(`Removed **${role.name}** from ${target.user}.`)] });
        await logModAction(interaction.guild!, { action: "Role Remove", target: target.user.tag, moderator: interaction.user.tag, reason: role.name });
      }
    } catch {
      await interaction.reply({ embeds: [errorEmbed("I couldn't change that role (missing permission or hierarchy).")], ephemeral: true });
    }
  },
};

export default command;
