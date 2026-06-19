import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  EmbedBuilder,
  ChannelType,
  type ChatInputCommandInteraction,
} from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { getGuildConfig, updateGuildConfig } from "../../db.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("verify")
    .setDescription("Member verification setup")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageRoles)
    .addSubcommand((sc) =>
      sc
        .setName("setup")
        .setDescription("Set the role given on verification")
        .addRoleOption((o) => o.setName("role").setDescription("Role to grant on verification").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("panel")
        .setDescription("Post the verification panel in a channel")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel to post the panel in").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guildId = interaction.guild!.id;

    if (sub === "setup") {
      const role = interaction.options.getRole("role", true);
      updateGuildConfig(guildId, { verify_role: role.id });
      await interaction.reply({ embeds: [successEmbed(`Verification role set to **${role.name}**.`)] });
      return;
    }

    const config = getGuildConfig(guildId);
    if (!config.verify_role) {
      await interaction.reply({ embeds: [errorEmbed("Set a verification role first with `/verify setup`.")], ephemeral: true });
      return;
    }

    const channel = interaction.options.getChannel("channel", true);
    const target = interaction.guild!.channels.cache.get(channel.id);
    if (!target?.isTextBased()) {
      await interaction.reply({ embeds: [errorEmbed("That channel can't receive messages.")], ephemeral: true });
      return;
    }

    updateGuildConfig(guildId, { verify_channel: channel.id });

    const embed = new EmbedBuilder()
      .setTitle("✅ Member Verification")
      .setDescription("Click the button below to verify yourself and gain access to the server.")
      .setColor(0x5865f2);
    const row = new ActionRowBuilder<ButtonBuilder>().addComponents(
      new ButtonBuilder().setCustomId("verify_member").setLabel("Verify").setStyle(ButtonStyle.Success)
    );

    await target.send({ embeds: [embed], components: [row] });
    await interaction.reply({ embeds: [successEmbed(`Verification panel posted in ${channel}.`)], ephemeral: true });
  },
};

export default command;
