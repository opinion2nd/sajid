import { EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle } from "discord.js";
import { db } from "../db.js";

interface PollRow {
  id: number;
  guild_id: string;
  channel_id: string;
  message_id: string;
  question: string;
  options: string;
  votes: string;
  created_at: number;
}

export function createPoll(guildId: string, channelId: string, messageId: string, question: string, options: string[]): number {
  const result = db
    .prepare("INSERT INTO polls (guild_id, channel_id, message_id, question, options, votes, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)")
    .run(guildId, channelId, messageId, question, JSON.stringify(options), JSON.stringify({}), Date.now());
  return Number(result.lastInsertRowid);
}

export function getPollById(id: number): PollRow | null {
  const row = db.prepare("SELECT * FROM polls WHERE id = ?").get(id) as PollRow | undefined;
  return row ?? null;
}

export function castVote(id: number, userId: string, optionIndex: number): PollRow | null {
  const poll = getPollById(id);
  if (!poll) return null;
  const votes: Record<string, number> = JSON.parse(poll.votes);
  votes[userId] = optionIndex;
  db.prepare("UPDATE polls SET votes = ? WHERE id = ?").run(JSON.stringify(votes), id);
  return { ...poll, votes: JSON.stringify(votes) };
}

export function buildPollEmbed(poll: PollRow) {
  const options: string[] = JSON.parse(poll.options);
  const votes: Record<string, number> = JSON.parse(poll.votes);
  const counts = new Array(options.length).fill(0);
  for (const idx of Object.values(votes)) counts[idx] = (counts[idx] ?? 0) + 1;
  const total = Object.values(votes).length;

  const description = options
    .map((opt, i) => {
      const pct = total > 0 ? Math.round((counts[i] / total) * 100) : 0;
      const barLength = Math.round(pct / 10);
      const bar = "█".repeat(barLength) + "░".repeat(10 - barLength);
      return `**${i + 1}. ${opt}**\n${bar} ${counts[i]} vote(s) (${pct}%)`;
    })
    .join("\n\n");

  return new EmbedBuilder()
    .setTitle(`📊 ${poll.question}`)
    .setDescription(description)
    .setColor(0x5865f2)
    .setFooter({ text: `${total} total vote(s)` });
}

export function buildPollButtonRow(pollId: number, options: string[]) {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    options.map((opt, i) =>
      new ButtonBuilder().setCustomId(`poll_${pollId}_${i}`).setLabel(opt.slice(0, 70)).setStyle(ButtonStyle.Primary)
    )
  );
}
