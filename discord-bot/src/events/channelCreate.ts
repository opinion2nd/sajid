import { AuditLogEvent, Events, type DMChannel, type GuildChannel } from "discord.js";
import { getGuildConfig } from "../db.js";
import { recordActionAndCheckNuke } from "../modules/antinuke.js";
import { punishNukeExecutor } from "../modules/nukeResponse.js";

export const name = Events.ChannelCreate;

export async function execute(channel: GuildChannel | DMChannel) {
  if (!("guild" in channel) || !channel.guild) return;
  const guild = channel.guild;
  const config = getGuildConfig(guild.id);
  if (!config.anti_nuke_enabled) return;

  const log = await guild.fetchAuditLogs({ type: AuditLogEvent.ChannelCreate, limit: 1 }).catch(() => null);
  const entry = log?.entries.first();
  if (!entry || !entry.executor || Date.now() - entry.createdTimestamp > 10_000) return;

  const exceeded = recordActionAndCheckNuke(guild.id, entry.executor.id, config.nuke_threshold, config.nuke_window_seconds);
  if (exceeded) {
    await punishNukeExecutor(guild, entry.executor.id, `Created ${config.nuke_threshold}+ channels in ${config.nuke_window_seconds}s`);
  }
}
