import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed } from "../../util/embeds.js";
import { getGuildConfig, updateGuildConfig } from "../../db.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("automod")
    .setDescription("Configure automatic moderation")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageGuild)
    .addSubcommand((sc) =>
      sc
        .setName("toggle")
        .setDescription("Enable or disable automod")
        .addBooleanOption((o) => o.setName("enabled").setDescription("Enable automod?").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("settings")
        .setDescription("Adjust automod filters")
        .addBooleanOption((o) => o.setName("anti_invite").setDescription("Block server invite links"))
        .addBooleanOption((o) => o.setName("anti_caps").setDescription("Block excessive caps"))
        .addBooleanOption((o) => o.setName("anti_spam").setDescription("Block duplicate-message spam"))
        .addIntegerOption((o) =>
          o.setName("max_mentions").setDescription("Max mentions per message (0 = unlimited)").setMinValue(0).setMaxValue(50)
        )
    )
    .addSubcommand((sc) => sc.setName("status").setDescription("Show current automod settings")),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guildId = interaction.guild!.id;

    if (sub === "toggle") {
      const enabled = interaction.options.getBoolean("enabled", true);
      updateGuildConfig(guildId, { automod_enabled: enabled ? 1 : 0 });
      await interaction.reply({ embeds: [successEmbed(`Automod is now **${enabled ? "enabled" : "disabled"}**.`)] });
      return;
    }

    if (sub === "settings") {
      const fields: Record<string, number> = {};
      const antiInvite = interaction.options.getBoolean("anti_invite");
      const antiCaps = interaction.options.getBoolean("anti_caps");
      const antiSpam = interaction.options.getBoolean("anti_spam");
      const maxMentions = interaction.options.getInteger("max_mentions");
      if (antiInvite !== null) fields.automod_anti_invite = antiInvite ? 1 : 0;
      if (antiCaps !== null) fields.automod_anti_caps = antiCaps ? 1 : 0;
      if (antiSpam !== null) fields.automod_anti_spam = antiSpam ? 1 : 0;
      if (maxMentions !== null) fields.automod_max_mentions = maxMentions;

      updateGuildConfig(guildId, fields);
      await interaction.reply({ embeds: [successEmbed("Automod settings updated.")] });
      return;
    }

    const config = getGuildConfig(guildId);
    const embed = new EmbedBuilder()
      .setTitle("Automod Settings")
      .setColor(0x5865f2)
      .addFields(
        { name: "Enabled", value: config.automod_enabled ? "Yes" : "No", inline: true },
        { name: "Anti-Invite", value: config.automod_anti_invite ? "Yes" : "No", inline: true },
        { name: "Anti-Caps", value: config.automod_anti_caps ? "Yes" : "No", inline: true },
        { name: "Anti-Spam", value: config.automod_anti_spam ? "Yes" : "No", inline: true },
        { name: "Max Mentions", value: String(config.automod_max_mentions), inline: true }
      );
    await interaction.reply({ embeds: [embed], ephemeral: true });
  },
};

export default command;
