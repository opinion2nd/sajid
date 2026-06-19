import { AuditLogEvent, Events, type Role } from "discord.js";
import { getGuildConfig } from "../db.js";
import { recordActionAndCheckNuke } from "../modules/antinuke.js";
import { punishNukeExecutor } from "../modules/nukeResponse.js";

export const name = Events.GuildRoleDelete;

export async function execute(role: Role) {
  const guild = role.guild;
  const config = getGuildConfig(guild.id);
  if (!config.anti_nuke_enabled) return;

  const log = await guild.fetchAuditLogs({ type: AuditLogEvent.RoleDelete, limit: 1 }).catch(() => null);
  const entry = log?.entries.first();
  if (!entry || !entry.executor || Date.now() - entry.createdTimestamp > 10_000) return;

  const exceeded = recordActionAndCheckNuke(guild.id, entry.executor.id, config.nuke_threshold, config.nuke_window_seconds);
  if (exceeded) {
    await punishNukeExecutor(guild, entry.executor.id, `Deleted ${config.nuke_threshold}+ roles in ${config.nuke_window_seconds}s`);
  }
}
