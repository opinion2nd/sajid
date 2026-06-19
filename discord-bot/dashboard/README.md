# Bot Dashboard

A Next.js web dashboard for managing the Discord bot in `../`. It reads and
writes the bot's SQLite database directly (`../data/bot.sqlite3` by default),
so it must run on the same machine/filesystem as the bot — there's no
separate API server.

## Features

- **Discord OAuth login** — sign in with Discord; only users with **Manage
  Server** (or server ownership) on a guild where the bot is installed can
  manage it.
- **Settings** — channels (mod log, welcome/leave, level-up, tickets,
  suggestions), roles (ticket support, verification), automod, and the
  security suite (anti-raid, anti-nuke, ghost-ping).
- **Tickets** — view open/closed tickets. "Mark Closed" only updates the
  database record; it does not delete the Discord channel (close tickets
  from Discord normally to clean up channels too).
- **Stats** — ticket/warning counts and the top-10 XP leaderboard.

Not included: the drag-and-drop "Components V2" message builder from the
original product — `/embed` in the bot covers basic custom embeds instead.

## Setup

1. `cd dashboard && npm install`
2. Copy `.env.example` to `.env.local` and fill in:
   - `DISCORD_CLIENT_ID` / `DISCORD_CLIENT_SECRET` — from the same
     [Discord application](https://discord.com/developers/applications) as the bot
   - `DISCORD_BOT_TOKEN` — same bot token as `../discord-bot`'s `.env` (used
     server-side to list the bot's guilds and fetch channel/role names)
   - `DISCORD_REDIRECT_URI` — must exactly match a redirect registered in the
     Developer Portal's OAuth2 settings, e.g. `http://localhost:3000/api/auth/callback`
   - `SESSION_SECRET` — any long random string (e.g. `openssl rand -hex 32`)
   - `BOT_DB_PATH` — path to the bot's sqlite file, relative to `dashboard/`
3. In the Developer Portal, add the redirect URI under OAuth2 → Redirects.
4. `npm run dev` and open `http://localhost:3000`.

Run the bot (`../`) at the same time so the database exists and the
dashboard's changes take effect live.
