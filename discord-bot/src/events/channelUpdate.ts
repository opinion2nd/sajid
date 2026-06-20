import { AuditLogEvent, Events, type DMChannel, type GuildChannel } from "discord.js";
import { getGuildConfig } from "../db.js";
import { recordActionAndCheckNuke } from "../modules/antinuke.js";
import { punishNukeExecutor } from "../modules/nukeResponse.js";

export const name = Events.ChannelUpdate;

// ChannelUpdate fires for any property change (rename, topic, slowmode, perms,
// etc). We only care about permission-overwrite nukes here, so we look up the
// matching overwrite audit log types and bail out if neither is fresh.
export async function execute(oldChannel: GuildChannel | DMChannel, newChannel: GuildChannel | DMChannel) {
  if (!("guild" in newChannel) || !newChannel.guild) return;
  const guild = newChannel.guild;
  const config = getGuildConfig(guild.id);
  if (!config.anti_nuke_enabled) return;

  const [createLog, updateLog] = await Promise.all([
    guild.fetchAuditLogs({ type: AuditLogEvent.ChannelOverwriteCreate, limit: 1 }).catch(() => null),
    guild.fetchAuditLogs({ type: AuditLogEvent.ChannelOverwriteUpdate, limit: 1 }).catch(() => null),
  ]);
  const candidates = [createLog?.entries.first(), updateLog?.entries.first()].filter(
    (e): e is NonNullable<typeof e> => Boolean(e)
  );
  const entry = candidates.sort((a, b) => b.createdTimestamp - a.createdTimestamp)[0];
  if (!entry || !entry.executor || Date.now() - entry.createdTimestamp > 10_000) return;

  const exceeded = recordActionAndCheckNuke(guild.id, entry.executor.id, config.nuke_threshold, config.nuke_window_seconds);
  if (exceeded) {
    await punishNukeExecutor(guild, entry.executor.id, `Edited ${config.nuke_threshold}+ channel permissions in ${config.nuke_window_seconds}s`);
  }
}
