import { SlashCommandBuilder, AttachmentBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { getUserXp, getRank, xpProgress } from "../../modules/leveling.js";
import { renderLevelCard } from "../../modules/levelCard.js";
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

    await interaction.deferReply();

    const { level, currentLevelXp, neededXp } = xpProgress(totalXp);
    const rank = getRank(guildId, target.id);

    const buffer = await renderLevelCard({
      username: target.username,
      avatarURL: target.displayAvatarURL({ extension: "png", size: 256 }),
      level,
      rank: rank?.rank,
      currentXp: currentLevelXp,
      neededXp,
      totalXp,
      mode: "rank",
    });

    const file = new AttachmentBuilder(buffer, { name: "rank.png" });
    await interaction.editReply({ files: [file] });
  },
};

export default command;
