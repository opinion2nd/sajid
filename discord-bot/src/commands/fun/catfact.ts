import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("catfact").setDescription("Get a random cat fact"),

  async execute(interaction: ChatInputCommandInteraction) {
    try {
      const res = await fetch("https://catfact.ninja/fact");
      if (!res.ok) throw new Error("bad response");
      const data = (await res.json()) as { fact: string };
      await interaction.reply({ embeds: [infoEmbed(`🐱 ${data.fact}`)] });
    } catch {
      await interaction.reply({ embeds: [errorEmbed("Couldn't fetch a cat fact right now, try again later.")], ephemeral: true });
    }
  },
};

export default command;
