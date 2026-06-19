import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("fact").setDescription("Get a random useless fact"),

  async execute(interaction: ChatInputCommandInteraction) {
    try {
      const res = await fetch("https://uselessfacts.jsph.pl/api/v2/facts/random?language=en");
      if (!res.ok) throw new Error("bad response");
      const data = (await res.json()) as { text: string };
      await interaction.reply({ embeds: [infoEmbed(`🧠 ${data.text}`)] });
    } catch {
      await interaction.reply({ embeds: [errorEmbed("Couldn't fetch a fact right now, try again later.")], ephemeral: true });
    }
  },
};

export default command;
