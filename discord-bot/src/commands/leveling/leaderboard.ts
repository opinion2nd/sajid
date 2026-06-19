import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { getLeaderboard } from "../../modules/leveling.js";
import { infoEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder().setName("leaderboard").setDescription("Show the server's top members by XP"),

  async execute(interaction: ChatInputCommandInteraction) {
    const top = getLeaderboard(interaction.guild!.id, 10);
    if (top.length === 0) {
      await interaction.reply({ embeds: [infoEmbed("No one has earned XP yet.")], ephemeral: true });
      return;
    }

    const medals = ["🥇", "🥈", "🥉"];
    const lines = top.map((row, i) => `${medals[i] ?? `**${i + 1}.**`} <@${row.user_id}> — Level ${row.level} (${row.xp} XP)`);

    const embed = new EmbedBuilder().setTitle("🏆 Leaderboard").setColor(0xfee75c).setDescription(lines.join("\n"));
    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
