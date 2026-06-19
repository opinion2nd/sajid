import {
  SlashCommandBuilder,
  PermissionFlagsBits,
  EmbedBuilder,
  ChannelType,
  type ChatInputCommandInteraction,
} from "discord.js";
import type { Command } from "../../types.js";
import { successEmbed, errorEmbed } from "../../util/embeds.js";
import { getTicketByChannel, closeTicket, addUserToTicket, removeUserFromTicket } from "../../modules/tickets.js";
import { getGuildConfig } from "../../db.js";
import { ActionRowBuilder, ButtonBuilder, ButtonStyle } from "discord.js";

const command: Command = {
  data: new SlashCommandBuilder()
    .setName("ticket")
    .setDescription("Manage the support ticket system")
    .setDefaultMemberPermissions(PermissionFlagsBits.ManageChannels)
    .addSubcommand((sc) =>
      sc
        .setName("panel")
        .setDescription("Post a ticket-opening panel in a channel")
        .addChannelOption((o) =>
          o.setName("channel").setDescription("Channel to post the panel in").setRequired(true).addChannelTypes(ChannelType.GuildText)
        )
        .addStringOption((o) => o.setName("title").setDescription("Panel title"))
        .addStringOption((o) => o.setName("description").setDescription("Panel description"))
    )
    .addSubcommand((sc) => sc.setName("close").setDescription("Close the ticket in this channel"))
    .addSubcommand((sc) =>
      sc
        .setName("add")
        .setDescription("Add a user to the current ticket")
        .addUserOption((o) => o.setName("user").setDescription("User to add").setRequired(true))
    )
    .addSubcommand((sc) =>
      sc
        .setName("remove")
        .setDescription("Remove a user from the current ticket")
        .addUserOption((o) => o.setName("user").setDescription("User to remove").setRequired(true))
    ),

  async execute(interaction: ChatInputCommandInteraction) {
    const sub = interaction.options.getSubcommand();
    const guild = interaction.guild!;

    if (sub === "panel") {
      const channel = interaction.options.getChannel("channel", true);
      const title = interaction.options.getString("title") ?? "Support Tickets";
      const description =
        interaction.options.getString("description") ?? "Click the button below to open a private support ticket.";

      const embed = new EmbedBuilder().setTitle(title).setDescription(description).setColor(0x5865f2);
      const row = new ActionRowBuilder<ButtonBuilder>().addComponents(
        new ButtonBuilder().setCustomId("ticket_open").setLabel("📩 Open Ticket").setStyle(ButtonStyle.Primary)
      );

      const target = guild.channels.cache.get(channel.id);
      if (!target?.isTextBased()) {
        await interaction.reply({ embeds: [errorEmbed("That channel can't receive messages.")], ephemeral: true });
        return;
      }
      await target.send({ embeds: [embed], components: [row] });
      await interaction.reply({ embeds: [successEmbed(`Ticket panel posted in ${channel}.`)], ephemeral: true });
      return;
    }

    const config = getGuildConfig(guild.id);
    if (!config.ticket_category && sub === "panel") return;

    if (sub === "close") {
      const ticket = getTicketByChannel(interaction.channelId);
      if (!ticket || ticket.status === "closed") {
        await interaction.reply({ embeds: [errorEmbed("This is not an open ticket channel.")], ephemeral: true });
        return;
      }
      await interaction.reply({ embeds: [successEmbed("🔒 Closing this ticket in 5 seconds...")] });
      closeTicket(interaction.channelId);
      setTimeout(() => {
        interaction.channel?.delete().catch(() => {});
      }, 5000);
      return;
    }

    const ticket = getTicketByChannel(interaction.channelId);
    if (!ticket) {
      await interaction.reply({ embeds: [errorEmbed("This command can only be used inside a ticket channel.")], ephemeral: true });
      return;
    }

    const user = interaction.options.getUser("user", true);
    if (sub === "add") {
      await addUserToTicket(guild, interaction.channelId, user.id);
      await interaction.reply({ embeds: [successEmbed(`Added ${user} to this ticket.`)] });
      return;
    }

    if (sub === "remove") {
      await removeUserFromTicket(guild, interaction.channelId, user.id);
      await interaction.reply({ embeds: [successEmbed(`Removed ${user} from this ticket.`)] });
    }
  },
};

export default command;
