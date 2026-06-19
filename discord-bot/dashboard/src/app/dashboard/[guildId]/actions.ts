"use server";

import { revalidatePath } from "next/cache";
import { requireGuildAccess } from "@/lib/auth";
import { updateGuildConfig, closeTicketRecord } from "@/lib/db";

function checkbox(formData: FormData, name: string): number {
  return formData.get(name) === "on" ? 1 : 0;
}

export async function updateModerationSettings(formData: FormData) {
  const guildId = String(formData.get("guildId"));
  await requireGuildAccess(guildId);

  updateGuildConfig(guildId, {
    automod_enabled: checkbox(formData, "automod_enabled"),
    automod_anti_invite: checkbox(formData, "automod_anti_invite"),
    automod_anti_caps: checkbox(formData, "automod_anti_caps"),
    automod_anti_spam: checkbox(formData, "automod_anti_spam"),
    automod_max_mentions: Number(formData.get("automod_max_mentions") || 5),
  });
  revalidatePath(`/dashboard/${guildId}`);
}

export async function updateSecuritySettings(formData: FormData) {
  const guildId = String(formData.get("guildId"));
  await requireGuildAccess(guildId);

  updateGuildConfig(guildId, {
    anti_raid_enabled: checkbox(formData, "anti_raid_enabled"),
    raid_join_threshold: Number(formData.get("raid_join_threshold") || 5),
    raid_window_seconds: Number(formData.get("raid_window_seconds") || 10),
    raid_account_age_days: Number(formData.get("raid_account_age_days") || 3),
    anti_nuke_enabled: checkbox(formData, "anti_nuke_enabled"),
    nuke_threshold: Number(formData.get("nuke_threshold") || 3),
    nuke_window_seconds: Number(formData.get("nuke_window_seconds") || 30),
    anti_ghostping_enabled: checkbox(formData, "anti_ghostping_enabled"),
  });
  revalidatePath(`/dashboard/${guildId}`);
}

export async function updateChannelSettings(formData: FormData) {
  const guildId = String(formData.get("guildId"));
  await requireGuildAccess(guildId);

  updateGuildConfig(guildId, {
    mod_log_channel: String(formData.get("mod_log_channel") || "") || null,
    welcome_channel: String(formData.get("welcome_channel") || "") || null,
    welcome_message: String(formData.get("welcome_message") || "") || null,
    leave_channel: String(formData.get("leave_channel") || "") || null,
    leave_message: String(formData.get("leave_message") || "") || null,
    levelup_channel: String(formData.get("levelup_channel") || "") || null,
    ticket_category: String(formData.get("ticket_category") || "") || null,
    ticket_log_channel: String(formData.get("ticket_log_channel") || "") || null,
    suggestion_channel: String(formData.get("suggestion_channel") || "") || null,
  });
  revalidatePath(`/dashboard/${guildId}`);
}

export async function updateRoleSettings(formData: FormData) {
  const guildId = String(formData.get("guildId"));
  await requireGuildAccess(guildId);

  updateGuildConfig(guildId, {
    ticket_support_role: String(formData.get("ticket_support_role") || "") || null,
    verify_role: String(formData.get("verify_role") || "") || null,
  });
  revalidatePath(`/dashboard/${guildId}`);
}

export async function closeTicketAction(formData: FormData) {
  const guildId = String(formData.get("guildId"));
  await requireGuildAccess(guildId);
  closeTicketRecord(Number(formData.get("ticketId")));
  revalidatePath(`/dashboard/${guildId}/tickets`);
}
