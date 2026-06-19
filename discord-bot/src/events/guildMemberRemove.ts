import { Events, type GuildMember, type PartialGuildMember } from "discord.js";
import { getGuildConfig } from "../db.js";
import { renderTemplate } from "../util/format.js";

export const name = Events.GuildMemberRemove;

export async function execute(member: GuildMember | PartialGuildMember) {
  const config = getGuildConfig(member.guild.id);
  if (!config.leave_channel) return;
  const channel = member.guild.channels.cache.get(config.leave_channel);
  if (!channel || !channel.isTextBased()) return;

  const template = config.leave_message || "{username} has left **{server}**. We're now {memberCount} members.";
  const text = renderTemplate(template, {
    userMention: member.toString(),
    username: member.user.username,
    serverName: member.guild.name,
    memberCount: member.guild.memberCount,
  });
  await channel.send(text).catch(() => {});
}
