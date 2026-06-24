import { Events, type GuildMember } from "discord.js";
import { getGuildConfig } from "../db.js";
import { renderTemplate } from "../util/format.js";
import { resolveUsedInviteAndCredit } from "../modules/invites.js";
import { recordJoinAndCheckRaid } from "../modules/antiraid.js";
import { logModAction } from "../modules/modlog.js";
import { restoreLicenseRoles } from "../modules/licenses.js";

export const name = Events.GuildMemberAdd;

const ACCOUNT_AGE_MS = (days: number) => days * 86_400_000;

export async function execute(member: GuildMember) {
  const config = getGuildConfig(member.guild.id);

  await resolveUsedInviteAndCredit(member.guild).catch(() => null);
  await restoreLicenseRoles(member).catch(() => {});

  if (config.anti_raid_enabled) {
    const isRaid = recordJoinAndCheckRaid(member.guild.id, config.raid_join_threshold, config.raid_window_seconds);
    if (isRaid) {
      const accountAge = Date.now() - member.user.createdTimestamp;
      const isNewAccount = accountAge < ACCOUNT_AGE_MS(config.raid_account_age_days);
      if (isNewAccount && member.kickable) {
        await member.kick("Anti-raid: rapid join burst + new account").catch(() => {});
        await logModAction(member.guild, {
          action: "Anti-Raid",
          target: member.user.tag,
          moderator: member.client.user.tag,
          reason: `Kicked during a join burst (account age ${Math.floor(accountAge / 86_400_000)}d).`,
        });
      } else {
        await logModAction(member.guild, {
          action: "Anti-Raid Alert",
          target: member.user.tag,
          moderator: member.client.user.tag,
          reason: "Join burst detected — review recent joins.",
        });
      }
      return;
    }
  }

  if (!config.welcome_channel) return;
  const channel = member.guild.channels.cache.get(config.welcome_channel);
  if (!channel || !channel.isTextBased()) return;

  const template = config.welcome_message || "Welcome {user} to **{server}**! We're now {memberCount} members.";
  const text = renderTemplate(template, {
    userMention: member.toString(),
    username: member.user.username,
    serverName: member.guild.name,
    memberCount: member.guild.memberCount,
  });
  await channel.send(text).catch(() => {});
}
