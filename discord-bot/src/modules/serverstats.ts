import { ChannelType, PermissionFlagsBits, type Guild } from "discord.js";
import { getGuildConfig } from "../db.js";

/**
 * Renames the configured member-count voice channel to reflect the current
 * member total. Safe to call on every join/leave — it no-ops if no channel is
 * configured or the channel was deleted.
 */
export async function updateMemberCountChannel(guild: Guild) {
  const config = getGuildConfig(guild.id);
  if (!config.member_count_channel) return;
  const channel = guild.channels.cache.get(config.member_count_channel);
  if (!channel) return;
  const name = `👥 Members: ${guild.memberCount}`;
  if (channel.name === name) return;
  await channel.setName(name).catch(() => {});
}

/** Creates a locked voice channel used purely as a live member-count display. */
export async function createMemberCountChannel(guild: Guild) {
  return guild.channels.create({
    name: `👥 Members: ${guild.memberCount}`,
    type: ChannelType.GuildVoice,
    permissionOverwrites: [
      { id: guild.roles.everyone.id, deny: [PermissionFlagsBits.Connect] },
    ],
  });
}
