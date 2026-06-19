import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, ChannelType, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("embed")
    .setDescription("Send a custom embed message")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageMessages)
    .addStringOption((o) => o.setName("description").setDescription("Embed body text").setRequired(true))
    .addChannelOption((o) =>
      o.setName("channel").setDescription("Channel to send to (defaults to here)").addChannelTypes(ChannelType.GuildText)
    )
    .addStringOption((o) => o.setName("title").setDescription("Embed title"))
    .addStringOption((o) => o.setName("color").setDescription("Hex color, e.g. #5865F2")),

  async execute(interaction: ChatInputCommandInteraction) {
    const description = interaction.options.getString("description", true);
    const title = interaction.options.getString("title");
    const colorInput = interaction.options.getString("color");
    const channelOpt = interaction.options.getChannel("channel");

    const target = channelOpt ? interaction.guild!.channels.cache.get(channelOpt.id) : interaction.channel;
    if (!target?.isSendable()) {
      await interaction.reply({ embeds: [errorEmbed("I can't send messages in that channel.")], ephemeral: true });
      return;
    }

    let color = 0x5865f2;
    if (colorInput) {
      const parsed = Number.parseInt(colorInput.replace("#", ""), 16);
      if (!Number.isNaN(parsed)) color = parsed;
    }

    const embed = new EmbedBuilder().setDescription(description).setColor(color);
    if (title) embed.setTitle(title);

    await target.send({ embeds: [embed] });
    await interaction.reply({ embeds: [successEmbed(`Embed sent to ${target}.`)], ephemeral: true });
  },
};

export default command;
