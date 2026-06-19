import { SlashCommandBuilder, ChannelType, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { getGuildConfig } from "../../db.js";
import { createSuggestion, getSuggestionById, buildSuggestionEmbed, buildSuggestionButtonRow } from "../../modules/suggestions.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("suggestion")
    .setDescription("Submit a suggestion for the server")
    .addStringOption((o) => o.setName("content").setDescription("Your suggestion").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const content = interaction.options.getString("content", true);
    const config = getGuildConfig(interaction.guild!.id);

    const channelId = config.suggestion_channel || interaction.channelId;
    const channel = interaction.guild!.channels.cache.get(channelId);
    if (!channel || channel.type !== ChannelType.GuildText || !channel.isSendable()) {
      await interaction.reply({ embeds: [errorEmbed("The suggestion channel isn't set up correctly. Ask an admin to configure it.")], ephemeral: true });
      return;
    }

    await interaction.deferReply({ ephemeral: true });
    const placeholder = await channel.send({ content: "Posting suggestion..." });

    const suggestionId = createSuggestion(interaction.guild!.id, channel.id, placeholder.id, interaction.user.id, content);
    const suggestion = getSuggestionById(suggestionId)!;

    await placeholder.edit({
      content: null,
      embeds: [buildSuggestionEmbed(suggestion, interaction.user.tag)],
      components: [buildSuggestionButtonRow(suggestionId)],
    });

    await interaction.editReply({ embeds: [successEmbed(`Your suggestion was posted in ${channel}.`)] });
  },
};

export default command;
