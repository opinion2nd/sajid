import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("meme").setDescription("Get a random meme"),

  async execute(interaction: ChatInputCommandInteraction) {
    try {
      const res = await fetch("https://meme-api.com/gimme");
      if (!res.ok) throw new Error("bad response");
      const data = (await res.json()) as { title: string; url: string; postLink: string };
      const embed = new EmbedBuilder().setTitle(data.title).setImage(data.url).setURL(data.postLink).setColor(0x5865f2);
      await interaction.reply({ embeds: [embed] });
    } catch {
      await interaction.reply({ embeds: [errorEmbed("Couldn't fetch a meme right now, try again later.")], ephemeral: true });
    }
  },
};

export default command;
