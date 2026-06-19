const MANAGE_GUILD = 0x20n;

export interface DiscordUser {
  id: string;
  username: string;
  avatar: string | null;
}

interface DiscordUserGuild {
  id: string;
  name: string;
  icon: string | null;
  owner: boolean;
  permissions: string;
}

export function getOAuthUrl(): string {
  const params = new URLSearchParams({
    client_id: process.env.DISCORD_CLIENT_ID!,
    redirect_uri: process.env.DISCORD_REDIRECT_URI!,
    response_type: "code",
    scope: "identify guilds",
  });
  return `https://discord.com/api/oauth2/authorize?${params.toString()}`;
}

export async function exchangeCodeForToken(code: string): Promise<string> {
  const res = await fetch("https://discord.com/api/oauth2/token", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
      client_id: process.env.DISCORD_CLIENT_ID!,
      client_secret: process.env.DISCORD_CLIENT_SECRET!,
      grant_type: "authorization_code",
      code,
      redirect_uri: process.env.DISCORD_REDIRECT_URI!,
    }),
  });
  if (!res.ok) throw new Error(`OAuth token exchange failed: ${res.status}`);
  const data = (await res.json()) as { access_token: string };
  return data.access_token;
}

export async function fetchDiscordUser(accessToken: string): Promise<DiscordUser> {
  const res = await fetch("https://discord.com/api/users/@me", {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!res.ok) throw new Error(`Failed to fetch Discord user: ${res.status}`);
  return (await res.json()) as DiscordUser;
}

async function fetchUserGuilds(accessToken: string): Promise<DiscordUserGuild[]> {
  const res = await fetch("https://discord.com/api/users/@me/guilds", {
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!res.ok) throw new Error(`Failed to fetch user guilds: ${res.status}`);
  return (await res.json()) as DiscordUserGuild[];
}

async function fetchBotGuildIds(): Promise<Set<string>> {
  const res = await fetch("https://discord.com/api/users/@me/guilds", {
    headers: { Authorization: `Bot ${process.env.DISCORD_BOT_TOKEN}` },
  });
  if (!res.ok) throw new Error(`Failed to fetch bot guilds: ${res.status}`);
  const guilds = (await res.json()) as { id: string }[];
  return new Set(guilds.map((g) => g.id));
}

export interface DiscordChannel {
  id: string;
  name: string;
  type: number;
}

export interface DiscordRole {
  id: string;
  name: string;
}

const TEXT_CHANNEL = 0;
const CATEGORY_CHANNEL = 4;

export async function fetchGuildChannels(guildId: string): Promise<{ text: DiscordChannel[]; categories: DiscordChannel[] }> {
  const res = await fetch(`https://discord.com/api/guilds/${guildId}/channels`, {
    headers: { Authorization: `Bot ${process.env.DISCORD_BOT_TOKEN}` },
  });
  if (!res.ok) return { text: [], categories: [] };
  const channels = (await res.json()) as DiscordChannel[];
  return {
    text: channels.filter((c) => c.type === TEXT_CHANNEL),
    categories: channels.filter((c) => c.type === CATEGORY_CHANNEL),
  };
}

export async function fetchGuildRoles(guildId: string): Promise<DiscordRole[]> {
  const res = await fetch(`https://discord.com/api/guilds/${guildId}/roles`, {
    headers: { Authorization: `Bot ${process.env.DISCORD_BOT_TOKEN}` },
  });
  if (!res.ok) return [];
  const roles = (await res.json()) as DiscordRole[];
  return roles.filter((r) => r.name !== "@everyone");
}

/** Returns guilds the user can manage (owner or Manage Guild permission) where the bot is also present. */
export async function getManageableGuilds(accessToken: string) {
  const [userGuilds, botGuildIds] = await Promise.all([fetchUserGuilds(accessToken), fetchBotGuildIds()]);
  return userGuilds
    .filter((g) => g.owner || (BigInt(g.permissions) & MANAGE_GUILD) === MANAGE_GUILD)
    .filter((g) => botGuildIds.has(g.id))
    .map((g) => ({ id: g.id, name: g.name, icon: g.icon }));
}
