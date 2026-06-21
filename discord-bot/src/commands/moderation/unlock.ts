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
    .setName("unlock")
    .setDescription("Unlock a previously locked channel")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageChannels)
    .addChannelOption((o) => o.setName("channel").setDescription("Channel to unlock (defaults to here)").addChannelTypes(ChannelType.GuildText)),

  async execute(interaction: ChatInputCommandInteraction) {
    const guild = interaction.guild!;
    const channel = (interaction.options.getChannel("channel") ?? interaction.channel) as TextChannel | null;

    if (!channel || channel.type !== ChannelType.GuildText) {
      await interaction.reply({ embeds: [errorEmbed("Pick a text channel to unlock.")], ephemeral: true });
      return;
    }

    await channel.permissionOverwrites.edit(guild.roles.everyone, { SendMessages: null }).catch(() => null);
    await interaction.reply({ embeds: [successEmbed(`🔓 Unlocked ${channel}.`)] });
    await logModAction(guild, { action: "Channel Unlock", target: `#${channel.name}`, moderator: interaction.user.tag, reason: "—" });
  },
};

export default command;
