import { Events, type Message, type PartialMessage } from "discord.js";
import { getGuildConfig } from "../db.js";
import { recordDeletedMessage } from "../modules/snipe.js";
import { logModAction } from "../modules/modlog.js";

export const name = Events.MessageDelete;

const GHOST_PING_WINDOW_MS = 60_000;

export async function execute(message: Message | PartialMessage) {
  if (!message.guild || !message.author || message.author.bot) return;

  recordDeletedMessage(message.channelId, {
    content: message.content ?? "*(no content — embed or attachment)*",
    authorTag: message.author.tag,
    authorAvatar: message.author.displayAvatarURL(),
    deletedAt: Date.now(),
  });

  const config = getGuildConfig(message.guild.id);
  if (!config.anti_ghostping_enabled) return;
  if (!message.mentions || message.mentions.users.size === 0) return;
  if (!message.createdTimestamp || Date.now() - message.createdTimestamp > GHOST_PING_WINDOW_MS) return;

  const mentioned = message.mentions.users.map((u) => u.tag).join(", ");
  await logModAction(message.guild, {
    action: "Ghost Ping Detected",
    target: message.author.tag,
    moderator: message.client.user.tag,
    reason: `Deleted a message mentioning: ${mentioned}`,
    extra: message.content || "*(no text content)*",
  });
}
