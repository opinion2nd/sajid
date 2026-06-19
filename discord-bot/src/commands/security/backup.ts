import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed, infoEmbed } from "../../util/embeds.js";
import { createBackup, listBackups, getBackup, restoreBackup } from "../../modules/backups.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("backup")
    .setDescription("Snapshot and restore server roles/channels")
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
    .addSubcommand((sc) => sc.setName("create").setDescription("Create a backup snapshot of roles and channels"))
    .addSubcommand((sc) => sc.setName("list").setDescription("List recent backups"))
    .addSubcommand((sc) =>
      sc
        .setName("restore")
        .setDescription("Restore missing roles/channels from a backup (never deletes or overwrites anything)")
        .addIntegerOption((o) => o.setName("id").setDescription("Backup ID (see /backup list)").setRequired(true))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guild = interaction.guild!;

    if (sub === "create") {
      await interaction.deferReply();
      const id = createBackup(guild);
      await interaction.editReply({ embeds: [successEmbed(`Backup #${id} created with the current roles and channels.`)] });
      return;
    }

    if (sub === "list") {
      const backups = listBackups(guild.id);
      if (backups.length === 0) {
        await interaction.reply({ embeds: [infoEmbed("No backups yet. Run `/backup create` first.")], ephemeral: true });
        return;
      }
      const embed = new EmbedBuilder()
        .setTitle("Recent Backups")
        .setColor(0x5865f2)
        .setDescription(backups.map((b) => `**#${b.id}** — <t:${Math.floor(b.created_at / 1000)}:F>`).join("\n"));
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    const id = interaction.options.getInteger("id", true);
    const backup = getBackup(guild.id, id);
    if (!backup) {
      await interaction.reply({ embeds: [errorEmbed(`No backup with ID #${id} found for this server.`)], ephemeral: true });
      return;
    }

    await interaction.deferReply();
    const { rolesCreated, channelsCreated } = await restoreBackup(guild, backup);
    await interaction.editReply({
      embeds: [
        successEmbed(
          `Restore complete from backup #${id}. Created **${rolesCreated}** missing role(s) and **${channelsCreated}** missing channel(s). Existing roles/channels were left untouched.`
        ),
      ],
    });
  },
};

export default command;
