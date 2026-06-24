import { EmbedBuilder } from "discord.js";

export const COLORS = {
  success: 0x57f287,
  error: 0xed4245,
  info: 0x5865f2,
  warn: 0xfee75c,
} as const;

export function successEmbed(description: string) {
  return new EmbedBuilder().setColor(COLORS.success).setDescription(`✅ ${description}`);
}

export function errorEmbed(description: string) {
  return new EmbedBuilder().setColor(COLORS.error).setDescription(`❌ ${description}`);
}

export function infoEmbed(description: string) {
  return new EmbedBuilder().setColor(COLORS.info).setDescription(description);
}
