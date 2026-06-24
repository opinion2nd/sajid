import { getStore, save } from "../db.js";

export function getGuildSettings(guildId: string) {
  return getStore().guildSettings[guildId] ?? {};
}

export function setWebhookUrl(guildId: string, url: string | null) {
  const store = getStore();
  const current = store.guildSettings[guildId] ?? {};
  store.guildSettings[guildId] = { ...current, webhookUrl: url ?? undefined };
  save();
}
