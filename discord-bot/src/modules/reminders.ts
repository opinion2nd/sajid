import type { Client } from "discord.js";
import { db } from "../db.js";

export interface Reminder {
  id: number;
  user_id: string;
  channel_id: string;
  guild_id: string;
  remind_at: number;
  message: string;
}

const scheduled = new Map<number, NodeJS.Timeout>();

async function fireReminder(client: Client, reminder: Reminder) {
  db.prepare("UPDATE reminders SET fired = 1 WHERE id = ?").run(reminder.id);
  try {
    const user = await client.users.fetch(reminder.user_id);
    await user.send(`⏰ Reminder: ${reminder.message}`);
  } catch {
    const channel = await client.channels.fetch(reminder.channel_id).catch(() => null);
    if (channel?.isSendable()) {
      await channel.send(`⏰ <@${reminder.user_id}> Reminder: ${reminder.message}`).catch(() => {});
    }
  }
}

export function scheduleReminder(client: Client, reminder: Reminder) {
  const existing = scheduled.get(reminder.id);
  if (existing) clearTimeout(existing);
  const delay = Math.max(reminder.remind_at - Date.now(), 0);
  const timeout = setTimeout(() => {
    scheduled.delete(reminder.id);
    fireReminder(client, reminder).catch((error) => console.error("Reminder fire error:", error));
  }, delay);
  scheduled.set(reminder.id, timeout);
}

export function createReminder(client: Client, userId: string, channelId: string, guildId: string, remindAt: number, message: string) {
  const result = db
    .prepare("INSERT INTO reminders (user_id, channel_id, guild_id, remind_at, message, created_at, fired) VALUES (?, ?, ?, ?, ?, ?, 0)")
    .run(userId, channelId, guildId, remindAt, message, Date.now());
  const reminder: Reminder = { id: Number(result.lastInsertRowid), user_id: userId, channel_id: channelId, guild_id: guildId, remind_at: remindAt, message };
  scheduleReminder(client, reminder);
}

export function rescheduleActiveReminders(client: Client) {
  const pending = db.prepare("SELECT * FROM reminders WHERE fired = 0").all() as Reminder[];
  for (const reminder of pending) {
    if (reminder.remind_at <= Date.now()) {
      fireReminder(client, reminder).catch((error) => console.error("Reminder fire error:", error));
    } else {
      scheduleReminder(client, reminder);
    }
  }
}
