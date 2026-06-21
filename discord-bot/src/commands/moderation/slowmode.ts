import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  ChannelType,
  type ChatInputCommandInteraction,
  type TextChannel,
} from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("slowmode")
    .setDescription("Set a channel's slowmode (per-user message delay)")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageChannels)
    .addIntegerOption((o) =>
      o.setName("seconds").setDescription("Delay in seconds (0 to disable)").setRequired(true).setMinValue(0).setMaxValue(21600)
    )
    .addChannelOption((o) => o.setName("channel").setDescription("Channel (defaults to here)").addChannelTypes(ChannelType.GuildText)),

  async execute(interaction: ChatInputCommandInteraction) {
    const seconds = interaction.options.getInteger("seconds", true);
    const channel = (interaction.options.getChannel("channel") ?? interaction.channel) as TextChannel | null;

    if (!channel || channel.type !== ChannelType.GuildText) {
      await interaction.reply({ embeds: [errorEmbed("Pick a text channel.")], ephemeral: true });
      return;
    }

    await channel.setRateLimitPerUser(seconds).catch(() => null);
    await interaction.reply({
      embeds: [successEmbed(seconds === 0 ? `Slowmode disabled in ${channel}.` : `Slowmode set to **${seconds}s** in ${channel}.`)],
    });
  },
};

export default command;
