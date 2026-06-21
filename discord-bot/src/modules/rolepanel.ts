import { EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle, type Guild } from "discord.js";

export interface PanelRole {
  id: string;
  name: string;
}

/**
 * Builds a self-role panel embed that lists each role with its live member
 * count, read from the guild's role cache.
 */
export function buildRolePanelEmbed(title: string, description: string, roles: PanelRole[], guild: Guild) {
  const lines = roles.map((r) => {
    const count = guild.roles.cache.get(r.id)?.members.size ?? 0;
    return `🔹 **${r.name}** — \`${count}\` member${count === 1 ? "" : "s"}`;
  });

  return new EmbedBuilder()
    .setTitle(`🎭 ${title}`)
    .setDescription(`${description}\n\n${lines.join("\n")}`)
    .setColor(0x5865f2)
    .setFooter({ text: "Click a button to add or remove a role • counts update live" });
}

export function buildRolePanelRows(roles: PanelRole[]) {
  const row = new ActionRowBuilder<ButtonBuilder>().addComponents(
    roles.map((role) =>
      new ButtonBuilder().setCustomId(`rolepanel_${role.id}`).setLabel(role.name).setStyle(ButtonStyle.Secondary)
    )
  );
  return [row];
}

/** Recovers the panel's role list from the buttons already on a message. */
export function rolesFromMessageComponents(components: readonly { components: readonly { customId?: string | null; label?: string | null }[] }[], guild: Guild): PanelRole[] {
  const roles: PanelRole[] = [];
  for (const row of components) {
    for (const comp of row.components) {
      if (comp.customId?.startsWith("rolepanel_")) {
        const id = comp.customId.slice("rolepanel_".length);
        roles.push({ id, name: guild.roles.cache.get(id)?.name ?? comp.label ?? "role" });
      }
    }
  }
  return roles;
}
