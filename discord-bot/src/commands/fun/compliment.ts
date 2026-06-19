import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed } from "../../util/embeds.js";

const COMPLIMENTS = [
  "is one of the kindest people in this server.",
  "always brightens up the chat.",
  "has impeccable taste.",
  "is way more talented than they let on.",
  "deserves a round of applause.",
  "is an absolute legend.",
  "has the best sense of humor here.",
  "is someone everyone should appreciate more.",
];

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("compliment")
    .setDescription("Send a compliment to someone")
    .addUserOption((o) => o.setName("user").setDescription("Who to compliment").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user", true);
    const compliment = COMPLIMENTS[Math.floor(Math.random() * COMPLIMENTS.length)];
    await interaction.reply({ embeds: [infoEmbed(`💖 ${target} ${compliment}`)] });
  },
};

export default command;
