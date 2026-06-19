import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { createGame2048, buildGame2048Embed, buildGame2048Rows } from "../../modules/games/twentyfortyeight.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("2048").setDescription("Play a game of 2048"),

  async execute(interaction: ChatInputCommandInteraction) {
    const game = createGame2048(interaction.user.id);
    await interaction.reply({ embeds: [buildGame2048Embed(game)], components: buildGame2048Rows(game) });
  },
};

export default command;
