import {
  AuditLogEvent,
  Events,
  type ForumChannel,
  type MediaChannel,
  type NewsChannel,
  type TextChannel,
  type VoiceChannel,
} from "discord.js";
import { getGuildConfig } from "../db.js";
import { recordActionAndCheckNuke } from "../modules/antinuke.js";
import { punishNukeExecutor } from "../modules/nukeResponse.js";

export const name = Events.WebhooksUpdate;

export async function execute(channel: TextChannel | NewsChannel | VoiceChannel | ForumChannel | MediaChannel) {
  const guild = channel.guild;
  const config = getGuildConfig(guild.id);
  if (!config.anti_nuke_enabled) return;

  const log = await guild.fetchAuditLogs({ type: AuditLogEvent.WebhookCreate, limit: 1 }).catch(() => null);
  const entry = log?.entries.first();
  if (!entry || !entry.executor || Date.now() - entry.createdTimestamp > 10_000) return;

  const exceeded = recordActionAndCheckNuke(guild.id, entry.executor.id, config.nuke_threshold, config.nuke_window_seconds);
  if (exceeded) {
    await punishNukeExecutor(guild, entry.executor.id, `Created ${config.nuke_threshold}+ webhooks in ${config.nuke_window_seconds}s`);
  }
}
