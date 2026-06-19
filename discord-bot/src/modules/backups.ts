import { ChannelType, type Guild } from "discord.js";
import { db } from "../db.js";

interface BackupRole {
  name: string;
  color: number;
  hoist: boolean;
  mentionable: boolean;
  permissions: string;
  position: number;
}

interface BackupChannel {
  name: string;
  type: ChannelType.GuildText | ChannelType.GuildVoice | ChannelType.GuildCategory;
  position: number;
  parentName: string | null;
}

interface BackupData {
  guildName: string;
  roles: BackupRole[];
  channels: BackupChannel[];
}

interface BackupRow {
  id: number;
  guild_id: string;
  created_at: number;
  data: string;
}

export function createBackup(guild: Guild): number {
  const roles: BackupRole[] = guild.roles.cache
    .filter((r) => r.id !== guild.id)
    .map((r) => ({
      name: r.name,
      color: r.color,
      hoist: r.hoist,
      mentionable: r.mentionable,
      permissions: r.permissions.bitfield.toString(),
      position: r.position,
    }));

  const channels: BackupChannel[] = guild.channels.cache
    .filter(
      (c) => c.type === ChannelType.GuildText || c.type === ChannelType.GuildVoice || c.type === ChannelType.GuildCategory
    )
    .map((c) => ({
      name: c.name,
      type: c.type as BackupChannel["type"],
      position: "position" in c ? c.position : 0,
      parentName: c.parent?.name ?? null,
    }));

  const data: BackupData = { guildName: guild.name, roles, channels };
  const result = db
    .prepare("INSERT INTO backups (guild_id, created_at, data) VALUES (?, ?, ?)")
    .run(guild.id, Date.now(), JSON.stringify(data));
  return Number(result.lastInsertRowid);
}

export function listBackups(guildId: string): BackupRow[] {
  return db.prepare("SELECT * FROM backups WHERE guild_id = ? ORDER BY created_at DESC LIMIT 10").all(guildId) as BackupRow[];
}

export function getBackup(guildId: string, id: number): BackupData | null {
  const row = db.prepare("SELECT * FROM backups WHERE guild_id = ? AND id = ?").get(guildId, id) as BackupRow | undefined;
  if (!row) return null;
  return JSON.parse(row.data) as BackupData;
}

/** Restores roles and channels that are missing by name. Never deletes or modifies existing roles/channels. */
export async function restoreBackup(guild: Guild, backup: BackupData) {
  let rolesCreated = 0;
  let channelsCreated = 0;

  const sortedRoles = [...backup.roles].sort((a, b) => a.position - b.position);
  for (const role of sortedRoles) {
    if (guild.roles.cache.some((r) => r.name === role.name)) continue;
    await guild.roles
      .create({
        name: role.name,
        color: role.color,
        hoist: role.hoist,
        mentionable: role.mentionable,
        permissions: BigInt(role.permissions),
      })
      .then(() => rolesCreated++)
      .catch(() => {});
  }

  const categories = backup.channels.filter((c) => c.type === ChannelType.GuildCategory);
  for (const cat of categories) {
    if (guild.channels.cache.some((c) => c.name === cat.name && c.type === ChannelType.GuildCategory)) continue;
    await guild.channels
      .create({ name: cat.name, type: ChannelType.GuildCategory })
      .then(() => channelsCreated++)
      .catch(() => {});
  }

  const nonCategories = backup.channels.filter((c) => c.type !== ChannelType.GuildCategory);
  for (const ch of nonCategories) {
    if (guild.channels.cache.some((c) => c.name === ch.name && c.type === ch.type)) continue;
    const parent = ch.parentName ? guild.channels.cache.find((c) => c.name === ch.parentName && c.type === ChannelType.GuildCategory) : null;
    await guild.channels
      .create({ name: ch.name, type: ch.type, parent: parent?.id })
      .then(() => channelsCreated++)
      .catch(() => {});
  }

  return { rolesCreated, channelsCreated };
}
