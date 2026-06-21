import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  ChannelType,
  type ChatInputCommandInteraction,
  type TextChannel,
} from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { logModAction } from "../../modules/modlog.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("lock")
    .setDescription("Lock a channel so members can't send messages")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageChannels)
    .addChannelOption((o) => o.setName("channel").setDescription("Channel to lock (defaults to here)").addChannelTypes(ChannelType.GuildText))
    .addStringOption((o) => o.setName("reason").setDescription("Reason")),

  async execute(interaction: ChatInputCommandInteraction) {
    const guild = interaction.guild!;
    const channel = (interaction.options.getChannel("channel") ?? interaction.channel) as TextChannel | null;
    const reason = interaction.options.getString("reason") ?? "No reason provided";

    if (!channel || channel.type !== ChannelType.GuildText) {
      await interaction.reply({ embeds: [errorEmbed("Pick a text channel to lock.")], ephemeral: true });
      return;
    }

    await channel.permissionOverwrites.edit(guild.roles.everyone, { SendMessages: false }).catch(() => null);
    await interaction.reply({ embeds: [successEmbed(`🔒 Locked ${channel}. Reason: ${reason}`)] });
    await logModAction(guild, { action: "Channel Lock", target: `#${channel.name}`, moderator: interaction.user.tag, reason });
  },
};

export default command;
