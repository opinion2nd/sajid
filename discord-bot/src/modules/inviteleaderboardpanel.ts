import { EmbedBuilder, type Guild } from "discord.js";
import { getGuildConfig, updateGuildConfig } from "../db.js";
import { getInviteLeaderboard } from "./invites.js";

const MEDALS = ["🥇", "🥈", "🥉"];

export function buildInviteLeaderboardEmbed(guildId: string): EmbedBuilder {
  const top = getInviteLeaderboard(guildId, 10);
  const lines = top.length
    ? top.map((row, i) => `${MEDALS[i] ?? `**${i + 1}.**`} <@${row.inviter_id}> — **${row.uses}** invite(s)`)
    : ["No tracked invites yet."];
  return new EmbedBuilder()
    .setTitle("📨 Live Invite Leaderboard")
    .setColor(0x5865f2)
    .setDescription(lines.join("\n"))
    .setFooter({ text: "Updates automatically as members join" })
    .setTimestamp();
}

/** Re-edits the registered invite-leaderboard panel message. Called on every join, naturally low-frequency. */
export async function refreshInviteLeaderboardPanel(guild: Guild) {
  const config = getGuildConfig(guild.id);
  if (!config.invites_channel || !config.invites_message) return;
  const channel = guild.channels.cache.get(config.invites_channel);
  if (!channel?.isTextBased()) return;
  const message = await channel.messages.fetch(config.invites_message).catch(() => null);
  if (!message) return;
  await message.edit({ embeds: [buildInviteLeaderboardEmbed(guild.id)] }).catch(() => {});
}

export function registerInviteLeaderboardPanel(guildId: string, channelId: string, messageId: string) {
  updateGuildConfig(guildId, { invites_channel: channelId, invites_message: messageId });
}
