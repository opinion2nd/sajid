import { Events, type GuildMember } from "discord.js";
import { restoreLicenseRoles } from "../modules/licenses.js";

export const name = Events.GuildMemberAdd;

export async function execute(member: GuildMember) {
  await restoreLicenseRoles(member).catch(() => {});
}
