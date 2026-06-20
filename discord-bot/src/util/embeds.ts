import { EmbedBuilder } from "discord.js";

export const COLORS = {
  success: 0x57f287,
  error: 0xed4245,
  info: 0x5865f2,
  warn: 0xfee75c,
  brand: 0x5865f2,
} as const;

export const BRAND_NAME = "Brother Craft";

/** Base embed with the bot's brand color + footer applied. Other helpers build on top of this. */
export function brandEmbed() {
  return new EmbedBuilder().setColor(COLORS.brand).setFooter({ text: BRAND_NAME });
}

export function successEmbed(description: string) {
  return brandEmbed().setColor(COLORS.success).setDescription(`✅ ${description}`);
}

export function errorEmbed(description: string) {
  return brandEmbed().setColor(COLORS.error).setDescription(`❌ ${description}`);
}

export function infoEmbed(description: string) {
  return brandEmbed().setColor(COLORS.info).setDescription(description);
}
