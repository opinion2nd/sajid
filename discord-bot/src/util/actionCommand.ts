import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../types.js";
import { errorEmbed } from "./embeds.js";
import { fetchActionGif } from "./gifAction.js";

export function createActionCommand(name: string, description: string, reaction: string, verb: string): Command {
  return {
    data: new SlashCommandBuilder()
      .setName(name)
      .setDescription(description)
      .addUserOption((o) => o.setName("user").setDescription("Who to target").setRequired(true)),

    async execute(interaction: ChatInputCommandInteraction) {
      const target = interaction.options.getUser("user", true);
      const gifUrl = await fetchActionGif(reaction);
      if (!gifUrl) {
        await interaction.reply({ embeds: [errorEmbed("Couldn't fetch a gif right now, try again later.")], ephemeral: true });
        return;
      }

      const embed = new EmbedBuilder()
        .setDescription(`${interaction.user} ${verb} ${target}!`)
        .setImage(gifUrl)
        .setColor(0x5865f2);
      await interaction.reply({ embeds: [embed] });
    },
  };
}
