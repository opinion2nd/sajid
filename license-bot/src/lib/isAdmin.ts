import type { ChatInputCommandInteraction } from "discord.js";

const ADMIN_USER_IDS = (process.env.LICENSE_BOT_ADMIN_USER_IDS ?? "").split(",").map((s) => s.trim()).filter(Boolean);
const ADMIN_ROLE_IDS = (process.env.LICENSE_BOT_ADMIN_ROLE_IDS ?? "").split(",").map((s) => s.trim()).filter(Boolean);

export function isAuthorized(interaction: ChatInputCommandInteraction): boolean {
  if (ADMIN_USER_IDS.includes(interaction.user.id)) return true;

  const member = interaction.member;
  if (member && "roles" in member) {
    const roleIds = Array.isArray(member.roles) ? member.roles : member.roles.cache.map((r) => r.id);
    if (ADMIN_ROLE_IDS.some((id) => roleIds.includes(id))) return true;
  }

  return false;
}
