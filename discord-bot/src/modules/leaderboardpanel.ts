import { EmbedBuilder, type Guild } from "discord.js";
import { getGuildConfig, updateGuildConfig } from "../db.js";
import { getLeaderboard } from "./leveling.js";

const MEDALS = ["🥇", "🥈", "🥉"];
const REFRESH_THROTTLE_MS = 15_000;
const lastRefresh = new Map<string, number>();

export function buildLeaderboardEmbed(guildId: string): EmbedBuilder {
  const top = getLeaderboard(guildId, 10);
  const lines = top.length
    ? top.map((row, i) => `${MEDALS[i] ?? `**${i + 1}.**`} <@${row.user_id}> — Level ${row.level} (${row.xp} XP)`)
    : ["No one has earned XP yet."];
  return new EmbedBuilder()
    .setTitle("🏆 Live XP Leaderboard")
    .setColor(0xfee75c)
    .setDescription(lines.join("\n"))
    .setFooter({ text: "Updates automatically as members chat" })
    .setTimestamp();
}

/** Re-edits the registered leaderboard panel message. Throttled per guild to avoid edit spam. */
export async function refreshLeaderboardPanel(guild: Guild) {
  const config = getGuildConfig(guild.id);
  if (!config.leaderboard_channel || !config.leaderboard_message) return;

  const last = lastRefresh.get(guild.id) ?? 0;
  if (Date.now() - last < REFRESH_THROTTLE_MS) return;
  lastRefresh.set(guild.id, Date.now());

  const channel = guild.channels.cache.get(config.leaderboard_channel);
  if (!channel?.isTextBased()) return;
  const message = await channel.messages.fetch(config.leaderboard_message).catch(() => null);
  if (!message) return;
  await message.edit({ embeds: [buildLeaderboardEmbed(guild.id)] }).catch(() => {});
}

export function registerLeaderboardPanel(guildId: string, channelId: string, messageId: string) {
  updateGuildConfig(guildId, { leaderboard_channel: channelId, leaderboard_message: messageId });
}
