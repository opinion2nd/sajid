import type { Collection, Guild, Invite } from "discord.js";
import { db } from "../db.js";

const cache = new Map<string, Collection<string, Invite>>();

export async function cacheGuildInvites(guild: Guild) {
  try {
    const invites = await guild.invites.fetch();
    cache.set(guild.id, invites);
  } catch {
    // Missing Manage Guild permission — invite tracking disabled for this guild.
  }
}

export async function resolveUsedInviteAndCredit(guild: Guild): Promise<string | null> {
  const before = cache.get(guild.id);
  let after: Collection<string, Invite>;
  try {
    after = await guild.invites.fetch();
  } catch {
    return null;
  }
  cache.set(guild.id, after);
  if (!before) return null;

  for (const [code, invite] of after) {
    const previous = before.get(code);
    const prevUses = previous?.uses ?? 0;
    if ((invite.uses ?? 0) > prevUses && invite.inviter) {
      creditInvite(guild.id, invite.inviter.id);
      return invite.inviter.id;
    }
  }
  return null;
}

function creditInvite(guildId: string, inviterId: string) {
  db.prepare(
    "INSERT INTO invites (guild_id, inviter_id, uses) VALUES (?, ?, 1) ON CONFLICT(guild_id, inviter_id) DO UPDATE SET uses = uses + 1"
  ).run(guildId, inviterId);
}

export function getInviteCount(guildId: string, userId: string): number {
  const row = db.prepare("SELECT uses FROM invites WHERE guild_id = ? AND inviter_id = ?").get(guildId, userId) as
    | { uses: number }
    | undefined;
  return row?.uses ?? 0;
}
