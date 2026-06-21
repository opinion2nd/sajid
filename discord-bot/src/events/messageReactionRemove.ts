import { Events, type MessageReaction, type PartialMessageReaction, type User, type PartialUser } from "discord.js";
import { handleStarReaction } from "../modules/starboard.js";
import { handleReactionRole } from "../modules/reactionroles.js";

export const name = Events.MessageReactionRemove;

export async function execute(
  reaction: MessageReaction | PartialMessageReaction,
  user: User | PartialUser
) {
  if (user.bot) return;
  await handleReactionRole(reaction, user, "remove").catch(() => {});
  await handleStarReaction(reaction).catch(() => {});
}
