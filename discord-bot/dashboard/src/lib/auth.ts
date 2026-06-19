import { redirect } from "next/navigation";
import { getSession, type SessionData } from "./session";

export async function requireGuildAccess(guildId: string): Promise<SessionData> {
  const session = await getSession();
  if (!session) redirect("/");
  if (!session.guilds.some((g) => g.id === guildId)) redirect("/dashboard");
  return session;
}
