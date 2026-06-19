import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed, errorEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("joke").setDescription("Get a random dad joke"),

  async execute(interaction: ChatInputCommandInteraction) {
    try {
      const res = await fetch("https://icanhazdadjoke.com/", { headers: { Accept: "application/json" } });
      if (!res.ok) throw new Error("bad response");
      const data = (await res.json()) as { joke: string };
      await interaction.reply({ embeds: [infoEmbed(`😂 ${data.joke}`)] });
    } catch {
      await interaction.reply({ embeds: [errorEmbed("Couldn't fetch a joke right now, try again later.")], ephemeral: true });
    }
  },
};

export default command;
