import { AuditLogEvent, Events, type GuildEmoji } from "discord.js";
import { getGuildConfig } from "../db.js";
import { recordActionAndCheckNuke } from "../modules/antinuke.js";
import { punishNukeExecutor } from "../modules/nukeResponse.js";

export const name = Events.GuildEmojiDelete;

export async function execute(emoji: GuildEmoji) {
  const guild = emoji.guild;
  const config = getGuildConfig(guild.id);
  if (!config.anti_nuke_enabled) return;

  const log = await guild.fetchAuditLogs({ type: AuditLogEvent.EmojiDelete, limit: 1 }).catch(() => null);
  const entry = log?.entries.first();
  if (!entry || !entry.executor || Date.now() - entry.createdTimestamp > 10_000) return;

  const exceeded = recordActionAndCheckNuke(guild.id, entry.executor.id, config.nuke_threshold, config.nuke_window_seconds);
  if (exceeded) {
    await punishNukeExecutor(guild, entry.executor.id, `Deleted ${config.nuke_threshold}+ emojis in ${config.nuke_window_seconds}s`);
  }
}
