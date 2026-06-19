import { SlashCommandBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { errorEmbed } from "../../util/embeds.js";
import { createConnectFourGame, buildConnectFourEmbed, buildConnectFourRow } from "../../modules/games/connectfour.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("connectfour")
    .setDescription("Play Connect Four against another member")
    .addUserOption((o) => o.setName("opponent").setDescription("Who to challenge").setRequired(true)),

  async execute(interaction: ChatInputCommandInteraction) {
    const opponent = interaction.options.getUser("opponent", true);
    if (opponent.id === interaction.user.id || opponent.bot) {
      await interaction.reply({ embeds: [errorEmbed("Pick a different human opponent.")], ephemeral: true });
      return;
    }

    const game = createConnectFourGame(interaction.user.id, opponent.id);
    await interaction.reply({ embeds: [buildConnectFourEmbed(game)], components: [buildConnectFourRow(game)] });
  },
};

export default command;
