import { Events, PermissionFlagsBits, AttachmentBuilder, type Message } from "discord.js";
import { getGuildConfig } from "../db.js";
import { checkAutomod } from "../modules/moderation.js";
import { maybeAddXp, getUserXp, getRank, xpProgress } from "../modules/leveling.js";
import { renderLevelCard } from "../modules/levelCard.js";
import { logModAction } from "../modules/modlog.js";
import { getAfk, clearAfk } from "../modules/afk.js";

export const name = Events.MessageCreate;

export async function execute(message: Message) {
  if (!message.guild || message.author.bot) return;

  const config = getGuildConfig(message.guild.id);

  if (getAfk(message.guild.id, message.author.id) && clearAfk(message.guild.id, message.author.id)) {
    if (message.channel.isSendable()) {
      await message.channel
        .send(`👋 Welcome back ${message.author}, I removed your AFK status.`)
        .then((m) => setTimeout(() => m.delete().catch(() => {}), 8000))
        .catch(() => {});
    }
  }

  if (message.mentions.users.size > 0 && message.channel.isSendable()) {
    for (const user of message.mentions.users.values()) {
      const afk = getAfk(message.guild.id, user.id);
      if (afk) {
        await message.channel.send(`💤 ${user.username} is AFK: ${afk.reason}`).catch(() => {});
      }
    }
  }

  if (config.automod_enabled) {
    const exempt = message.member?.permissions.has(PermissionFlagsBits.ManageMessages) ?? false;
    if (!exempt) {
      const violation = checkAutomod(message.guild.id, message.author.id, message.content, message.mentions.users.size, {
        anti_invite: Boolean(config.automod_anti_invite),
        anti_caps: Boolean(config.automod_anti_caps),
        anti_spam: Boolean(config.automod_anti_spam),
        max_mentions: config.automod_max_mentions,
      });
      if (violation) {
        await message.delete().catch(() => {});
        if (message.channel.isSendable()) {
          await message.channel
            .send(`${message.author}, your message was removed: ${violation.reason}`)
            .then((m) => setTimeout(() => m.delete().catch(() => {}), 6000))
            .catch(() => {});
        }
        await logModAction(message.guild, {
          action: "Automod",
          target: message.author.tag,
          moderator: message.client.user.tag,
          reason: violation.reason,
        });
        return;
      }
    }
  }

  const result = maybeAddXp(message.guild.id, message.author.id);
  if (result?.leveledUp) {
    const channelId = config.levelup_channel || message.channel.id;
    const channel = message.guild.channels.cache.get(channelId);
    if (channel?.isTextBased()) {
      try {
        const totalXp = getUserXp(message.guild.id, message.author.id);
        const { level, currentLevelXp, neededXp } = xpProgress(totalXp);
        const rank = getRank(message.guild.id, message.author.id);
        const buffer = await renderLevelCard({
          username: message.author.username,
          avatarURL: message.author.displayAvatarURL({ extension: "png", size: 256 }),
          level,
          rank: rank?.rank,
          currentXp: currentLevelXp,
          neededXp,
          totalXp,
          mode: "levelup",
        });
        const file = new AttachmentBuilder(buffer, { name: "levelup.png" });
        await channel.send({ content: `🎉 ${message.author}`, files: [file] });
      } catch (err) {
        // Fall back to a plain text message if card rendering fails.
        console.error("[levelCard] render failed:", err);
        await channel.send(`🎉 ${message.author} leveled up to **level ${result.newLevel}**!`).catch(() => {});
      }
    }
  }
}
