import { EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle } from "discord.js";
import { db } from "../db.js";

interface SuggestionRow {
  id: number;
  guild_id: string;
  channel_id: string;
  message_id: string;
  user_id: string;
  content: string;
  upvotes: string;
  downvotes: string;
  created_at: number;
}

export function createSuggestion(guildId: string, channelId: string, messageId: string, userId: string, content: string): number {
  const result = db
    .prepare(
      "INSERT INTO suggestions (guild_id, channel_id, message_id, user_id, content, upvotes, downvotes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
    )
    .run(guildId, channelId, messageId, userId, content, JSON.stringify([]), JSON.stringify([]), Date.now());
  return Number(result.lastInsertRowid);
}

export function getSuggestionById(id: number): SuggestionRow | null {
  const row = db.prepare("SELECT * FROM suggestions WHERE id = ?").get(id) as SuggestionRow | undefined;
  return row ?? null;
}

export function castSuggestionVote(id: number, userId: string, direction: "up" | "down"): SuggestionRow | null {
  const suggestion = getSuggestionById(id);
  if (!suggestion) return null;

  let upvotes: string[] = JSON.parse(suggestion.upvotes);
  let downvotes: string[] = JSON.parse(suggestion.downvotes);
  upvotes = upvotes.filter((u) => u !== userId);
  downvotes = downvotes.filter((u) => u !== userId);

  if (direction === "up") upvotes.push(userId);
  else downvotes.push(userId);

  db.prepare("UPDATE suggestions SET upvotes = ?, downvotes = ? WHERE id = ?").run(
    JSON.stringify(upvotes),
    JSON.stringify(downvotes),
    id
  );
  return { ...suggestion, upvotes: JSON.stringify(upvotes), downvotes: JSON.stringify(downvotes) };
}

export function buildSuggestionEmbed(suggestion: SuggestionRow, authorTag: string) {
  const upvotes: string[] = JSON.parse(suggestion.upvotes);
  const downvotes: string[] = JSON.parse(suggestion.downvotes);
  return new EmbedBuilder()
    .setTitle("💡 New Suggestion")
    .setDescription(suggestion.content)
    .setFooter({ text: `Suggested by ${authorTag}` })
    .setColor(0x5865f2)
    .addFields(
      { name: "👍 Upvotes", value: String(upvotes.length), inline: true },
      { name: "👎 Downvotes", value: String(downvotes.length), inline: true }
    );
}

export function buildSuggestionButtonRow(id: number) {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder().setCustomId(`suggestion_up_${id}`).setLabel("👍 Upvote").setStyle(ButtonStyle.Success),
    new ButtonBuilder().setCustomId(`suggestion_down_${id}`).setLabel("👎 Downvote").setStyle(ButtonStyle.Danger)
  );
}
