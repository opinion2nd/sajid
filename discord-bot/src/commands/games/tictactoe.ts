import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { errorEmbed } from "../../util/embeds.js";
import { createTicTacToeGame, buildTicTacToeEmbed, buildTicTacToeRows } from "../../modules/games/tictactoe.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("tictactoe")
    .setDescription("Play tic-tac-toe against another member")
    .addUserOption((o) => o.setName("opponent").setDescription("Who to challenge").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const opponent = interaction.options.getUser("opponent", true);
    if (opponent.id === interaction.user.id || opponent.bot) {
      await interaction.reply({ embeds: [errorEmbed("Pick a different human opponent.")], ephemeral: true });
      return;
    }

    const game = createTicTacToeGame(interaction.user.id, opponent.id);
    await interaction.reply({ embeds: [buildTicTacToeEmbed(game)], components: buildTicTacToeRows(game) });
  },
};

export default command;
