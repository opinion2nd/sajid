import { EmbedBuilder, type Guild } from "discord.js";
import { getGuildConfig } from "../db.js";

export interface ModActionDetails {
  action: string;
  target: string;
  moderator: string;
  reason: string;
  extra?: string;
}

export async function logModAction(guild: Guild, details: ModActionDetails) {
  const config = getGuildConfig(guild.id);
  if (!config.mod_log_channel) return;
  const channel = guild.channels.cache.get(config.mod_log_channel);
  if (!channel || !channel.isTextBased()) return;

  const embed = new EmbedBuilder()
    .setTitle(`Moderation — ${details.action}`)
    .addFields(
      { name: "Target", value: details.target, inline: true },
      { name: "Moderator", value: details.moderator, inline: true },
      { name: "Reason", value: details.reason || "No reason provided" }
    )
    .setColor(0xed4245)
    .setTimestamp();
  if (details.extra) embed.addFields({ name: "Details", value: details.extra });

  await channel.send({ embeds: [embed] }).catch(() => {});
}
