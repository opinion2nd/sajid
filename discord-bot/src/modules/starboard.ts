import { EmbedBuilder, type MessageReaction, type PartialMessageReaction } from "discord.js";
import { db, getGuildConfig } from "../db.js";

export const STAR_EMOJI = "⭐";

interface StarEntry {
  star_message_id: string;
}

/**
 * Posts or updates a starred message in the starboard channel once it reaches
 * the configured threshold. Safe to call on every ⭐ reaction change.
 */
export async function handleStarReaction(reaction: MessageReaction | PartialMessageReaction) {
  if (reaction.partial) {
    await reaction.fetch().catch(() => null);
  }
  if (reaction.emoji.name !== STAR_EMOJI) return;

  const message = reaction.message;
  if (!message.guild) return;
  const config = getGuildConfig(message.guild.id);
  if (!config.starboard_channel) return;

  // Don't let the starboard star its own posts.
  if (message.channelId === config.starboard_channel) return;

  if (message.partial) {
    await message.fetch().catch(() => null);
  }

  const count = message.reactions.cache.get(STAR_EMOJI)?.count ?? 0;
  const channel = message.guild.channels.cache.get(config.starboard_channel);
  if (!channel?.isTextBased()) return;

  const existing = db
    .prepare("SELECT star_message_id FROM starboard WHERE guild_id = ? AND original_message_id = ?")
    .get(message.guild.id, message.id) as StarEntry | undefined;

  if (count < config.starboard_threshold) {
    // Below threshold: remove a previously posted entry if it exists.
    if (existing) {
      await channel.messages.fetch(existing.star_message_id).then((m) => m.delete()).catch(() => {});
      db.prepare("DELETE FROM starboard WHERE guild_id = ? AND original_message_id = ?").run(message.guild.id, message.id);
    }
    return;
  }

  const author = message.author;
  const embed = new EmbedBuilder()
    .setColor(0xffac33)
    .setAuthor({ name: author?.tag ?? "Unknown", iconURL: author?.displayAvatarURL() })
    .setDescription(message.content || "*(no text)*")
    .addFields({ name: "Source", value: `[Jump to message](${message.url})` })
    .setFooter({ text: `${count} ⭐` })
    .setTimestamp(message.createdTimestamp);

  const image = message.attachments.find((a) => a.contentType?.startsWith("image/"));
  if (image) embed.setImage(image.url);

  const header = `${STAR_EMOJI} **${count}** | <#${message.channelId}>`;

  if (existing) {
    const starMsg = await channel.messages.fetch(existing.star_message_id).catch(() => null);
    if (starMsg) {
      await starMsg.edit({ content: header, embeds: [embed] }).catch(() => {});
      return;
    }
  }

  const sent = await channel.send({ content: header, embeds: [embed] }).catch(() => null);
  if (sent) {
    db.prepare(
      "INSERT OR REPLACE INTO starboard (guild_id, original_message_id, star_message_id) VALUES (?, ?, ?)"
    ).run(message.guild.id, message.id, sent.id);
  }
}
