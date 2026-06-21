import { type MessageReaction, type PartialMessageReaction, type User, type PartialUser } from "discord.js";
import { db } from "../db.js";

export interface ReactionRole {
  id: number;
  guild_id: string;
  channel_id: string;
  message_id: string;
  emoji: string;
  role_id: string;
}

/** Normalizes an emoji to a stable key: custom emojis use their ID, unicode uses the char. */
export function emojiKey(emoji: { id?: string | null; name?: string | null }): string {
  return emoji.id ?? emoji.name ?? "";
}

export function addReactionRole(guildId: string, channelId: string, messageId: string, emoji: string, roleId: string) {
  db.prepare(
    "INSERT INTO reaction_roles (guild_id, channel_id, message_id, emoji, role_id) VALUES (?, ?, ?, ?, ?)"
  ).run(guildId, channelId, messageId, emoji, roleId);
}

export function removeReactionRole(guildId: string, messageId: string, emoji: string) {
  return db
    .prepare("DELETE FROM reaction_roles WHERE guild_id = ? AND message_id = ? AND emoji = ?")
    .run(guildId, messageId, emoji);
}

export function getReactionRole(guildId: string, messageId: string, emoji: string): ReactionRole | undefined {
  return db
    .prepare("SELECT * FROM reaction_roles WHERE guild_id = ? AND message_id = ? AND emoji = ?")
    .get(guildId, messageId, emoji) as ReactionRole | undefined;
}

/** Adds or removes the mapped role when a tracked reaction changes. */
export async function handleReactionRole(
  reaction: MessageReaction | PartialMessageReaction,
  user: User | PartialUser,
  action: "add" | "remove"
) {
  if (user.bot) return;
  if (reaction.partial) await reaction.fetch().catch(() => null);
  const guild = reaction.message.guild;
  if (!guild) return;

  const mapping = getReactionRole(guild.id, reaction.message.id, emojiKey(reaction.emoji));
  if (!mapping) return;

  const member = await guild.members.fetch(user.id).catch(() => null);
  const role = guild.roles.cache.get(mapping.role_id);
  if (!member || !role) return;

  if (action === "add") {
    await member.roles.add(role).catch(() => {});
  } else {
    await member.roles.remove(role).catch(() => {});
  }
}
