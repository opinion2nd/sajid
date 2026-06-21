import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  ChannelType,
  type ChatInputCommandInteraction,
} from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { buildRolePanelEmbed, buildRolePanelRows, type PanelRole } from "../../modules/rolepanel.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("role-panel")
    .setDescription("Post a self-assignable role panel (click a button to toggle a role)")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageRoles)
    .addChannelOption((o) =>
      o.setName("channel").setDescription("Channel to post the panel in").setRequired(true).addChannelTypes(ChannelType.GuildText)
    )
    .addStringOption((o) => o.setName("title").setDescription("Panel title").setRequired(true))
    .addRoleOption((o) => o.setName("role1").setDescription("First role").setRequired(true))
    .addRoleOption((o) => o.setName("role2").setDescription("Second role"))
    .addRoleOption((o) => o.setName("role3").setDescription("Third role"))
    .addRoleOption((o) => o.setName("role4").setDescription("Fourth role"))
    .addStringOption((o) => o.setName("description").setDescription("Panel description")),

  async execute(interaction: ChatInputCommandInteraction) {
    const channel = interaction.options.getChannel("channel", true);
    const title = interaction.options.getString("title", true);
    const description = interaction.options.getString("description") ?? "Click a button below to add or remove a role.";

    const roles: PanelRole[] = [1, 2, 3, 4]
      .map((n) => interaction.options.getRole(`role${n}`))
      .filter((r): r is NonNullable<typeof r> => Boolean(r))
      .map((r) => ({ id: r.id, name: r.name }));

    const guild = interaction.guild!;
    const target = guild.channels.cache.get(channel.id);
    if (!target?.isTextBased()) {
      await interaction.reply({ embeds: [errorEmbed("That channel can't receive messages.")], ephemeral: true });
      return;
    }

    // Warm the member cache so the initial counts are accurate.
    await guild.members.fetch().catch(() => {});

    const embed = buildRolePanelEmbed(title, description, roles, guild);
    await target.send({ embeds: [embed], components: buildRolePanelRows(roles) });
    await interaction.reply({ embeds: [successEmbed(`Role panel posted in ${channel}.`)], ephemeral: true });
  },
};

export default command;
