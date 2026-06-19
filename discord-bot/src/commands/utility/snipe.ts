import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed } from "../../util/embeds.js";
import { getSnipe } from "../../modules/snipe.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("snipe").setDescription("Show the most recently deleted message in this channel"),

  async execute(interaction: ChatInputCommandInteraction) {
    const sniped = getSnipe(interaction.channelId);
    if (!sniped) {
      await interaction.reply({ embeds: [infoEmbed("There's nothing to snipe in this channel.")], ephemeral: true });
      return;
    }

    const embed = new EmbedBuilder()
      .setAuthor({ name: sniped.authorTag, iconURL: sniped.authorAvatar })
      .setDescription(sniped.content)
      .setColor(0x5865f2)
      .setFooter({ text: `Deleted ${Math.floor((Date.now() - sniped.deletedAt) / 1000)}s ago` });
    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
