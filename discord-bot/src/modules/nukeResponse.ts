import type { Guild } from "discord.js";
import { resetNukeTracking, isNukeWhitelisted } from "./antinuke.js";
import { logModAction } from "./modlog.js";

/** Strips roles from (or bans) the suspected nuke executor and logs the action. Owner & whitelisted users are never touched. */
export async function punishNukeExecutor(guild: Guild, executorId: string, reason: string) {
  resetNukeTracking(guild.id, executorId);
  if (
    executorId === guild.ownerId ||
    executorId === guild.client.user.id ||
    isNukeWhitelisted(guild.id, executorId)
  )
    return;

  const member = await guild.members.fetch(executorId).catch(() => null);
  let actionTaken = "Could not reach the member to act (may have left).";

  if (member) {
    if (member.bannable) {
      await member.ban({ reason: `Anti-nuke: ${reason}` }).catch(() => {});
      actionTaken = "Member was banned.";
    } else if (member.manageable) {
      await member.roles.set([]).catch(() => {});
      actionTaken = "Member's roles were stripped (not bannable due to role hierarchy).";
    } else {
      actionTaken = "Member outranks the bot — no automatic action could be taken.";
    }
  }

  await logModAction(guild, {
    action: "Anti-Nuke",
    target: member?.user.tag ?? executorId,
    moderator: guild.client.user.tag,
    reason,
    extra: actionTaken,
  });
}
