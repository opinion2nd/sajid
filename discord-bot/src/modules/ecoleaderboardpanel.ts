import { EmbedBuilder, type Guild } from "discord.js";
import { getGuildConfig, updateGuildConfig } from "../db.js";
import { getEconomyLeaderboard, CURRENCY } from "./economy.js";

const MEDALS = ["🥇", "🥈", "🥉"];
const REFRESH_THROTTLE_MS = 10_000;
const lastRefresh = new Map<string, number>();

export function buildBaltopEmbed(guildId: string): EmbedBuilder {
  const top = getEconomyLeaderboard(guildId, 10);
  const lines = top.length
    ? top.map((row, i) => `${MEDALS[i] ?? `**${i + 1}.**`} <@${row.user_id}> — ${CURRENCY} ${row.balance.toLocaleString()}`)
    : ["No one has any coins yet."];
  return new EmbedBuilder()
    .setTitle("💰 Live Richest Members")
    .setColor(0xf1c40f)
    .setDescription(lines.join("\n"))
    .setFooter({ text: "Updates automatically as balances change" })
    .setTimestamp();
}

/** Re-edits the registered baltop panel message. Throttled per guild to avoid edit spam. */
export async function refreshBaltopPanel(guild: Guild) {
  const config = getGuildConfig(guild.id);
  if (!config.baltop_channel || !config.baltop_message) return;

  const last = lastRefresh.get(guild.id) ?? 0;
  if (Date.now() - last < REFRESH_THROTTLE_MS) return;
  lastRefresh.set(guild.id, Date.now());

  const channel = guild.channels.cache.get(config.baltop_channel);
  if (!channel?.isTextBased()) return;
  const message = await channel.messages.fetch(config.baltop_message).catch(() => null);
  if (!message) return;
  await message.edit({ embeds: [buildBaltopEmbed(guild.id)] }).catch(() => {});
}

export function registerBaltopPanel(guildId: string, channelId: string, messageId: string) {
  updateGuildConfig(guildId, { baltop_channel: channelId, baltop_message: messageId });
}
