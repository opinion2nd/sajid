import { SlashCommandBuilder, PermissionFlagsBits, EmbedBuilder, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed } from "../../util/embeds.js";
import { getGuildConfig, updateGuildConfig } from "../../db.js";
import { addNukeWhitelist, removeNukeWhitelist, listNukeWhitelist } from "../../modules/antinuke.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("security")
    .setDescription("Configure anti-raid, anti-nuke, and anti-ghost-ping protection")
    .setDefaultMemberPermissions(PermissionFlagsBits.Administrator)
    .addSubcommandGroup((g) =>
      g
        .setName("raid")
        .setDescription("Anti-raid (join burst) protection")
        .addSubcommand((sc) =>
          sc.setName("toggle").setDescription("Enable or disable anti-raid").addBooleanOption((o) =>
            o.setName("enabled").setDescription("Enable anti-raid?").setRequired(true)
          )
        )
        .addSubcommand((sc) =>
          sc
            .setName("settings")
            .setDescription("Adjust anti-raid thresholds")
            .addIntegerOption((o) => o.setName("join_threshold").setDescription("Joins within the window that trigger a raid alert").setMinValue(2).setMaxValue(50))
            .addIntegerOption((o) => o.setName("window_seconds").setDescription("Time window in seconds").setMinValue(2).setMaxValue(300))
            .addIntegerOption((o) => o.setName("account_age_days").setDescription("Accounts younger than this (days) get auto-kicked during a raid").setMinValue(0).setMaxValue(365))
        )
    )
    .addSubcommandGroup((g) =>
      g
        .setName("nuke")
        .setDescription("Anti-nuke (mass destructive action) protection")
        .addSubcommand((sc) =>
          sc.setName("toggle").setDescription("Enable or disable anti-nuke").addBooleanOption((o) =>
            o.setName("enabled").setDescription("Enable anti-nuke?").setRequired(true)
          )
        )
        .addSubcommand((sc) =>
          sc
            .setName("settings")
            .setDescription("Adjust anti-nuke thresholds")
            .addIntegerOption((o) => o.setName("threshold").setDescription("Destructive actions within the window that trigger punishment").setMinValue(2).setMaxValue(50))
            .addIntegerOption((o) => o.setName("window_seconds").setDescription("Time window in seconds").setMinValue(5).setMaxValue(600))
        )
    )
    .addSubcommandGroup((g) =>
      g
        .setName("whitelist")
        .setDescription("Trusted users that anti-nuke will never punish")
        .addSubcommand((sc) =>
          sc
            .setName("add")
            .setDescription("Add a trusted user to the anti-nuke whitelist")
            .addUserOption((o) => o.setName("user").setDescription("User to trust").setRequired(true))
        )
        .addSubcommand((sc) =>
          sc
            .setName("remove")
            .setDescription("Remove a user from the anti-nuke whitelist")
            .addUserOption((o) => o.setName("user").setDescription("User to remove").setRequired(true))
        )
        .addSubcommand((sc) => sc.setName("list").setDescription("List whitelisted users"))
    )
    .addSubcommand((sc) =>
      sc
        .setName("ghostping")
        .setDescription("Enable or disable ghost-ping detection")
        .addBooleanOption((o) => o.setName("enabled").setDescription("Enable ghost-ping detection?").setRequired(true))
    )
    .addSubcommand((sc) => sc.setName("status").setDescription("Show current security settings")),

  async execute(interaction: ChatInputCommandInteraction) {
    const group = interaction.options.getSubcommandGroup(false);
    const sub = interaction.options.getSubcommand();
    const guildId = interaction.guild!.id;

    if (group === "raid" && sub === "toggle") {
      const enabled = interaction.options.getBoolean("enabled", true);
      updateGuildConfig(guildId, { anti_raid_enabled: enabled ? 1 : 0 });
      await interaction.reply({ embeds: [successEmbed(`Anti-raid is now **${enabled ? "enabled" : "disabled"}**.`)] });
      return;
    }

    if (group === "raid" && sub === "settings") {
      const fields: Record<string, number> = {};
      const joinThreshold = interaction.options.getInteger("join_threshold");
      const windowSeconds = interaction.options.getInteger("window_seconds");
      const accountAgeDays = interaction.options.getInteger("account_age_days");
      if (joinThreshold !== null) fields.raid_join_threshold = joinThreshold;
      if (windowSeconds !== null) fields.raid_window_seconds = windowSeconds;
      if (accountAgeDays !== null) fields.raid_account_age_days = accountAgeDays;
      updateGuildConfig(guildId, fields);
      await interaction.reply({ embeds: [successEmbed("Anti-raid settings updated.")] });
      return;
    }

    if (group === "nuke" && sub === "toggle") {
      const enabled = interaction.options.getBoolean("enabled", true);
      updateGuildConfig(guildId, { anti_nuke_enabled: enabled ? 1 : 0 });
      await interaction.reply({ embeds: [successEmbed(`Anti-nuke is now **${enabled ? "enabled" : "disabled"}**.`)] });
      return;
    }

    if (group === "nuke" && sub === "settings") {
      const fields: Record<string, number> = {};
      const threshold = interaction.options.getInteger("threshold");
      const windowSeconds = interaction.options.getInteger("window_seconds");
      if (threshold !== null) fields.nuke_threshold = threshold;
      if (windowSeconds !== null) fields.nuke_window_seconds = windowSeconds;
      updateGuildConfig(guildId, fields);
      await interaction.reply({ embeds: [successEmbed("Anti-nuke settings updated.")] });
      return;
    }

    if (group === "whitelist" && sub === "add") {
      const user = interaction.options.getUser("user", true);
      addNukeWhitelist(guildId, user.id, interaction.user.id);
      await interaction.reply({ embeds: [successEmbed(`${user} is now whitelisted — anti-nuke will never punish them.`)] });
      return;
    }

    if (group === "whitelist" && sub === "remove") {
      const user = interaction.options.getUser("user", true);
      const removed = removeNukeWhitelist(guildId, user.id);
      await interaction.reply({
        embeds: [successEmbed(removed ? `${user} was removed from the anti-nuke whitelist.` : `${user} was not on the whitelist.`)],
      });
      return;
    }

    if (group === "whitelist" && sub === "list") {
      const ids = listNukeWhitelist(guildId);
      const value = ids.length ? ids.map((id) => `<@${id}>`).join("\n") : "_No users are whitelisted._";
      const embed = new EmbedBuilder().setTitle("Anti-Nuke Whitelist").setColor(0x5865f2).setDescription(value);
      await interaction.reply({ embeds: [embed], ephemeral: true });
      return;
    }

    if (sub === "ghostping") {
      const enabled = interaction.options.getBoolean("enabled", true);
      updateGuildConfig(guildId, { anti_ghostping_enabled: enabled ? 1 : 0 });
      await interaction.reply({ embeds: [successEmbed(`Ghost-ping detection is now **${enabled ? "enabled" : "disabled"}**.`)] });
      return;
    }

    const config = getGuildConfig(guildId);
    const embed = new EmbedBuilder()
      .setTitle("Security Settings")
      .setColor(0x5865f2)
      .addFields(
        { name: "Anti-Raid", value: config.anti_raid_enabled ? "Enabled" : "Disabled", inline: true },
        { name: "Raid Threshold", value: `${config.raid_join_threshold} joins / ${config.raid_window_seconds}s`, inline: true },
        { name: "Raid Account Age", value: `< ${config.raid_account_age_days}d gets kicked`, inline: true },
        { name: "Anti-Nuke", value: config.anti_nuke_enabled ? "Enabled" : "Disabled", inline: true },
        { name: "Nuke Threshold", value: `${config.nuke_threshold} actions / ${config.nuke_window_seconds}s`, inline: true },
        { name: "Nuke Whitelist", value: `${listNukeWhitelist(guildId).length} trusted user(s)`, inline: true },
        { name: "Ghost-Ping Detection", value: config.anti_ghostping_enabled ? "Enabled" : "Disabled", inline: true }
      );
    await interaction.reply({ embeds: [embed], ephemeral: true });
  },
};

export default command;
