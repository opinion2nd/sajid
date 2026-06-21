import { ChannelType, PermissionFlagsBits, EmbedBuilder, type Guild, type TextChannel } from "discord.js";
import { db, getGuildConfig, updateGuildConfig } from "../db.js";

export interface Ticket {
  id: number;
  guild_id: string;
  channel_id: string;
  user_id: string;
  status: string;
  created_at: number;
  closed_at: number | null;
}

export async function createTicketChannel(
  guild: Guild,
  userId: string
): Promise<{ existing: boolean; channel: TextChannel | null }> {
  const existing = db
    .prepare("SELECT * FROM tickets WHERE guild_id = ? AND user_id = ? AND status = 'open'")
    .get(guild.id, userId) as Ticket | undefined;
  if (existing) {
    const channel = guild.channels.cache.get(existing.channel_id);
    return { existing: true, channel: (channel as TextChannel) ?? null };
  }

  const config = getGuildConfig(guild.id);
  const member = guild.members.cache.get(userId);
  const safeName = (member?.user.username ?? userId).toLowerCase().replace(/[^a-z0-9-]/g, "").slice(0, 80);

  const overwrites = [
    { id: guild.roles.everyone.id, deny: [PermissionFlagsBits.ViewChannel] },
    {
      id: userId,
      allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.ReadMessageHistory],
    },
    {
      id: guild.client.user!.id,
      allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.ManageChannels],
    },
  ];
  if (config.ticket_support_role) {
    overwrites.push({
      id: config.ticket_support_role,
      allow: [PermissionFlagsBits.ViewChannel, PermissionFlagsBits.SendMessages, PermissionFlagsBits.ReadMessageHistory],
    });
  }

  const channel = await guild.channels.create({
    name: `ticket-${safeName || "user"}`,
    type: ChannelType.GuildText,
    parent: config.ticket_category || undefined,
    permissionOverwrites: overwrites,
  });

  db.prepare(
    "INSERT INTO tickets (guild_id, channel_id, user_id, status, created_at) VALUES (?, ?, ?, 'open', ?)"
  ).run(guild.id, channel.id, userId, Date.now());

  return { existing: false, channel };
}

export function getTicketByChannel(channelId: string): Ticket | undefined {
  return db.prepare("SELECT * FROM tickets WHERE channel_id = ?").get(channelId) as Ticket | undefined;
}

export function closeTicket(channelId: string) {
  db.prepare("UPDATE tickets SET status = 'closed', closed_at = ? WHERE channel_id = ?").run(Date.now(), channelId);
}

export async function addUserToTicket(guild: Guild, channelId: string, userId: string) {
  const channel = guild.channels.cache.get(channelId);
  if (!channel || channel.type !== ChannelType.GuildText) return false;
  await channel.permissionOverwrites.edit(userId, {
    ViewChannel: true,
    SendMessages: true,
    ReadMessageHistory: true,
  });
  return true;
}

export async function removeUserFromTicket(guild: Guild, channelId: string, userId: string) {
  const channel = guild.channels.cache.get(channelId);
  if (!channel || channel.type !== ChannelType.GuildText) return false;
  await channel.permissionOverwrites.delete(userId);
  return true;
}

export function getOpenTicketCount(guildId: string): number {
  const row = db.prepare("SELECT COUNT(*) AS n FROM tickets WHERE guild_id = ? AND status = 'open'").get(guildId) as {
    n: number;
  };
  return row.n;
}

export function ticketPanelFooter(openCount: number): string {
  return `🎟️ Open tickets: ${openCount}`;
}

/**
 * Re-edits the stored ticket panel message so its footer shows the current
 * open-ticket count. No-ops if no panel is registered or it was deleted.
 */
export async function refreshTicketPanel(guild: Guild) {
  const config = getGuildConfig(guild.id);
  if (!config.ticket_panel_channel || !config.ticket_panel_message) return;
  const channel = guild.channels.cache.get(config.ticket_panel_channel);
  if (!channel?.isTextBased()) return;
  const message = await channel.messages.fetch(config.ticket_panel_message).catch(() => null);
  if (!message || !message.embeds[0]) return;
  const embed = EmbedBuilder.from(message.embeds[0]).setFooter({ text: ticketPanelFooter(getOpenTicketCount(guild.id)) });
  await message.edit({ embeds: [embed] }).catch(() => {});
}

export function registerTicketPanel(guildId: string, channelId: string, messageId: string) {
  updateGuildConfig(guildId, { ticket_panel_channel: channelId, ticket_panel_message: messageId });
}
