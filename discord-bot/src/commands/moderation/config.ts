import { SlashCommandBuilder, PermissionFlagsBits, ChannelType, type ChatInputCommandInteraction } from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed } from "../../util/embeds.js";
import { updateGuildConfig } from "../../db.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("config")
    .setDescription("Configure server settings for the bot")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageGuild)
    .addSubcommand((sc) =>
      sc
        .setName("modlog")
        .setDescription("Set the moderation log channel")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel for mod logs").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
    )
    .addSubcommand((sc) =>
      sc
        .setName("welcome")
        .setDescription("Set the welcome channel and message")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel for welcome messages").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
        .addStringOption((o) =>
          o.setName("message").setDescription("Use {user}, {username}, {server}, {memberCount}")
        )
    )
    .addSubcommand((sc) =>
      sc
        .setName("leave")
        .setDescription("Set the leave channel and message")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel for leave messages").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
        .addStringOption((o) =>
          o.setName("message").setDescription("Use {user}, {username}, {server}, {memberCount}")
        )
    )
    .addSubcommand((sc) =>
      sc
        .setName("levelup")
        .setDescription("Set the level-up announcement channel")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel for level-up messages").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
    )
    .addSubcommand((sc) =>
      sc
        .setName("ticketcategory")
        .setDescription("Set the category where new ticket channels are created")
        .addChannelOption((o) =>
          o.setName("category").setDescription("Ticket category").setRequired(true).addChannelTypes(ChannelType.GuildCategory)
        )
    )
    .addSubcommand((sc) =>
      sc
        .setName("ticketlog")
        .setDescription("Set the ticket log channel")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel for ticket logs").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
    )
    .addSubcommand((sc) =>
      sc
        .setName("ticketrole")
        .setDescription("Set the support role that can see all tickets")
        .addRoleOption((o) => o.setName("role").setDescription("Support role").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("suggestions")
        .setDescription("Set the channel where /suggestion posts go")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel for suggestions").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guildId = interaction.guild!.id;

    switch (sub) {
      case "modlog": {
        const channel = interaction.options.getChannel("channel", true);
        updateGuildConfig(guildId, { mod_log_channel: channel.id });
        await interaction.reply({ embeds: [successEmbed(`Mod log channel set to ${channel}.`)] });
        return;
      }
      case "welcome": {
        const channel = interaction.options.getChannel("channel", true);
        const message = interaction.options.getString("message");
        updateGuildConfig(guildId, { welcome_channel: channel.id, ...(message ? { welcome_message: message } : {}) });
        await interaction.reply({ embeds: [successEmbed(`Welcome channel set to ${channel}.`)] });
        return;
      }
      case "leave": {
        const channel = interaction.options.getChannel("channel", true);
        const message = interaction.options.getString("message");
        updateGuildConfig(guildId, { leave_channel: channel.id, ...(message ? { leave_message: message } : {}) });
        await interaction.reply({ embeds: [successEmbed(`Leave channel set to ${channel}.`)] });
        return;
      }
      case "levelup": {
        const channel = interaction.options.getChannel("channel", true);
        updateGuildConfig(guildId, { levelup_channel: channel.id });
        await interaction.reply({ embeds: [successEmbed(`Level-up channel set to ${channel}.`)] });
        return;
      }
      case "ticketcategory": {
        const category = interaction.options.getChannel("category", true);
        updateGuildConfig(guildId, { ticket_category: category.id });
        await interaction.reply({ embeds: [successEmbed(`Ticket category set to **${category.name}**.`)] });
        return;
      }
      case "ticketlog": {
        const channel = interaction.options.getChannel("channel", true);
        updateGuildConfig(guildId, { ticket_log_channel: channel.id });
        await interaction.reply({ embeds: [successEmbed(`Ticket log channel set to ${channel}.`)] });
        return;
      }
      case "ticketrole": {
        const role = interaction.options.getRole("role", true);
        updateGuildConfig(guildId, { ticket_support_role: role.id });
        await interaction.reply({ embeds: [successEmbed(`Ticket support role set to **${role.name}**.`)] });
        return;
      }
      case "suggestions": {
        const channel = interaction.options.getChannel("channel", true);
        updateGuildConfig(guildId, { suggestion_channel: channel.id });
        await interaction.reply({ embeds: [successEmbed(`Suggestion channel set to ${channel}.`)] });
        return;
      }
    }
  },
};

export default command;
