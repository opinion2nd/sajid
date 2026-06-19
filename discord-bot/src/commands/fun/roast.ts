import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { infoEmbed } from "../../util/embeds.js";

const ROASTS = [
  "is the human equivalent of a buffering icon.",
  "has the rizz of a dial-up modem.",
  "took longer to load than this bot's database.",
  "is proof that loading screens can be sentient.",
  "has main character energy but extra side character.",
  "argues with the GPS and still gets lost.",
  "is the reason captchas exist.",
  "has a Wi-Fi signal stronger than their comebacks.",
];

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("roast")
    .setDescription("Playfully roast someone (all in good fun)")
    .addUserOption((o) => o.setName("user").setDescription("Who to roast").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user", true);
    const roast = ROASTS[Math.floor(Math.random() * ROASTS.length)];
    await interaction.reply({ embeds: [infoEmbed(`🔥 ${target} ${roast}`)] });
  },
};

export default command;
