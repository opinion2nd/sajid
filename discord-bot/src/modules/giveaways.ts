import {
  ActionRowBuilder,
  ButtonBuilder,
  ButtonStyle,
  EmbedBuilder,
  type Client,
} from "discord.js";
import { db } from "../db.js";

export interface Giveaway {
  id: number;
  guild_id: string;
  channel_id: string;
  message_id: string;
  host_id: string;
  prize: string;
  winner_count: number;
  end_at: number;
  ended: number;
  entries: string;
}

export function buildGiveawayEmbed(prize: string, winnerCount: number, endAt: number, hostId: string, entryCount: number) {
  return new EmbedBuilder()
    .setTitle("🎉 Giveaway 🎉")
    .setDescription(
      `**Prize:** ${prize}\n**Winners:** ${winnerCount}\n**Ends:** <t:${Math.floor(endAt / 1000)}:R>\n**Hosted by:** <@${hostId}>\n**Entries:** ${entryCount}`
    )
    .setColor(0x5865f2);
}

export function buildGiveawayButtonRow(giveawayId: number, disabled = false) {
  return new ActionRowBuilder<ButtonBuilder>().addComponents(
    new ButtonBuilder()
      .setCustomId(`giveaway_enter_${giveawayId}`)
      .setLabel("🎉 Enter")
      .setStyle(ButtonStyle.Success)
      .setDisabled(disabled)
  );
}

export async function endGiveaway(client: Client, giveawayId: number, isReroll = false) {
  const giveaway = db.prepare("SELECT * FROM giveaways WHERE id = ?").get(giveawayId) as Giveaway | undefined;
  if (!giveaway) return null;

  const entries: string[] = JSON.parse(giveaway.entries);
  const pool = [...entries];
  const winners: string[] = [];
  for (let i = 0; i < giveaway.winner_count && pool.length > 0; i++) {
    const idx = Math.floor(Math.random() * pool.length);
    winners.push(pool.splice(idx, 1)[0]);
  }

  if (!isReroll) {
    db.prepare("UPDATE giveaways SET ended = 1 WHERE id = ?").run(giveawayId);
  }

  try {
    const channel = await client.channels.fetch(giveaway.channel_id);
    if (channel?.isSendable()) {
      const message = await channel.messages.fetch(giveaway.message_id).catch(() => null);
      if (message && message.embeds[0]) {
        const embed = EmbedBuilder.from(message.embeds[0]).setDescription(
          winners.length
            ? `**Prize:** ${giveaway.prize}\n**Winner(s):** ${winners.map((w) => `<@${w}>`).join(", ")}\n**Hosted by:** <@${giveaway.host_id}>`
            : `**Prize:** ${giveaway.prize}\n**No valid entries — no winner.**\n**Hosted by:** <@${giveaway.host_id}>`
        );
        await message.edit({ embeds: [embed], components: [buildGiveawayButtonRow(giveawayId, true)] });
      }
      if (winners.length) {
        await channel.send(`🎉 Congratulations ${winners.map((w) => `<@${w}>`).join(", ")}! You won **${giveaway.prize}**!`);
      } else if (!isReroll) {
        await channel.send(`No one entered the giveaway for **${giveaway.prize}**.`);
      }
    }
  } catch (error) {
    console.warn("Failed to announce giveaway result:", error);
  }

  return winners;
}

const scheduled = new Map<number, NodeJS.Timeout>();

export function scheduleGiveawayEnd(client: Client, giveawayId: number, endAt: number) {
  const existing = scheduled.get(giveawayId);
  if (existing) clearTimeout(existing);
  const delay = Math.max(endAt - Date.now(), 0);
  const timeout = setTimeout(() => {
    scheduled.delete(giveawayId);
    endGiveaway(client, giveawayId).catch((error) => console.error("Giveaway end error:", error));
  }, delay);
  scheduled.set(giveawayId, timeout);
}

export function rescheduleActiveGiveaways(client: Client) {
  const active = db.prepare("SELECT id, end_at FROM giveaways WHERE ended = 0").all() as Pick<Giveaway, "id" | "end_at">[];
  for (const g of active) {
    if (g.end_at <= Date.now()) {
      endGiveaway(client, g.id).catch((error) => console.error("Giveaway end error:", error));
    } else {
      scheduleGiveawayEnd(client, g.id, g.end_at);
    }
  }
}
