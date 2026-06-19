import { SlashCommandBuilder, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { getUserXp, getRank, xpProgress } from "../../modules/leveling.js";
import { infoEmbed } from "../../util/embeds.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("rank")
    .setDescription("Show your or another member's level and rank")
    .addUserOption((o) => o.setName("user").setDescription("Member to check")),

  async execute(interaction: ChatInputCommandInteraction) {
    const target = interaction.options.getUser("user") ?? interaction.user;
    const guildId = interaction.guild!.id;
    const totalXp = getUserXp(guildId, target.id);

    if (totalXp === 0) {
      await interaction.reply({ embeds: [infoEmbed(`**${target.tag}** hasn't earned any XP yet.`)], ephemeral: true });
      return;
    }

    const { level, currentLevelXp, neededXp } = xpProgress(totalXp);
    const rank = getRank(guildId, target.id);

    const embed = new EmbedBuilder()
      .setTitle(`${target.username}'s Rank`)
      .setThumbnail(target.displayAvatarURL())
      .setColor(0x5865f2)
      .addFields(
        { name: "Level", value: String(level), inline: true },
        { name: "Rank", value: rank ? `#${rank.rank} / ${rank.total}` : "—", inline: true },
        { name: "XP", value: `${currentLevelXp} / ${neededXp}`, inline: true }
      );
    await interaction.reply({ embeds: [embed] });
  },
};

export default command;
