import { AuditLogEvent, Events, AttachmentBuilder, type GuildMember, type PartialGuildMember } from "discord.js";
import { getGuildConfig } from "../db.js";
import { renderTemplate } from "../util/format.js";
import { recordActionAndCheckNuke } from "../modules/antinuke.js";
import { punishNukeExecutor } from "../modules/nukeResponse.js";
import { renderWelcomeCard } from "../modules/welcomeCard.js";

export const name = Events.GuildMemberRemove;

export async function execute(member: GuildMember | PartialGuildMember) {
  const guild = member.guild;
  const config = getGuildConfig(guild.id);

  if (config.anti_nuke_enabled) {
    const log = await guild.fetchAuditLogs({ type: AuditLogEvent.MemberKick, limit: 1 }).catch(() => null);
    const entry = log?.entries.first();
    // MemberRemove fires on both kicks and voluntary leaves; match the audit
    // log target back to this specific member so leaves aren't miscounted as kicks.
    if (entry && entry.executor && entry.target?.id === member.id && Date.now() - entry.createdTimestamp < 10_000) {
      const exceeded = recordActionAndCheckNuke(guild.id, entry.executor.id, config.nuke_threshold, config.nuke_window_seconds);
      if (exceeded) {
        await punishNukeExecutor(guild, entry.executor.id, `Kicked ${config.nuke_threshold}+ members in ${config.nuke_window_seconds}s`);
      }
    }
  }

  if (!config.leave_channel) return;
  const channel = guild.channels.cache.get(config.leave_channel);
  if (!channel || !channel.isTextBased()) return;

  const template = config.leave_message || "{username} has left **{server}**. We're now {memberCount} members.";
  const text = renderTemplate(template, {
    userMention: member.toString(),
    username: member.user.username,
    serverName: member.guild.name,
    memberCount: member.guild.memberCount,
  });

  try {
    const buffer = await renderWelcomeCard({
      username: member.user.username,
      avatarURL: member.user.displayAvatarURL({ extension: "png", size: 256 }),
      serverName: member.guild.name,
      memberCount: member.guild.memberCount,
      mode: "goodbye",
    });
    const file = new AttachmentBuilder(buffer, { name: "goodbye.png" });
    await channel.send({ content: text, files: [file] });
  } catch (err) {
    console.error("[goodbyeCard] render failed:", err);
    await channel.send(text).catch(() => {});
  }
}
