# Flaming Bot — Multi-Purpose Discord Bot

A self-contained Discord bot (discord.js v14 + TypeScript + SQLite).
No external services required — all data is stored locally in `data/bot.sqlite3`.

## Modules

- **Moderation** — `/kick`, `/ban`, `/unban`, `/timeout set|remove`, `/warn add|list|clear`, `/purge`
- **Automod** — `/automod toggle|settings|status` (anti-invite, anti-caps, anti-spam, mention spam limit)
- **Tickets** — `/ticket panel|close|add|remove`, button-based ticket creation with per-user private channels
- **Leveling** — automatic XP on messages, `/rank`, `/leaderboard`
- **Giveaways** — `/giveaway start|end|reroll|list`, button-based entry
- **Welcome / Leave / Reaction roles** — `/config welcome|leave`, `/role-panel`
- **Server config** — `/config modlog|welcome|leave|levelup|ticketcategory|ticketlog|ticketrole|suggestions`
- **Security suite** — `/security raid|nuke|ghostping|status` (anti-raid join-burst kicking, anti-nuke audit-log-based punishment for mass channel/role deletes and bans, ghost-ping detection), `/verify setup|panel` (button-based member verification), `/backup create|list|restore` (additive-only role/channel snapshot restore)
- **AFK** — `/afk`, auto-clears on your next message, replies when someone mentions an AFK user
- **Reminders** — `/remind` (DMs you, falls back to channel ping), persists across restarts
- **Invite tracking** — `/invites`, credits whoever's invite a new member used
- **Snipe** — `/snipe` shows the last deleted message in a channel (5 min TTL)
- **Polls & Suggestions** — `/poll` (up to 5 options, live vote-tally buttons), `/suggestion` (upvote/downvote buttons)
- **Custom embeds** — `/embed` for staff-sent announcements
- **General** — `/ping`, `/botinfo`, `/serverinfo`, `/userinfo`, `/avatar`
- **Fun** — `/8ball`, `/coinflip`, `/roll`, `/rps`, `/meme`, `/joke`, `/fact`, `/catfact`, `/hug`, `/kiss`, `/slap`, `/dance`, `/compliment`, `/roast`, `/ship` (gif-based commands use the free, keyless [otakugifs.xyz](https://otakugifs.xyz) API; meme/joke/fact commands use other free keyless APIs)
- **Games** — `/tictactoe`, `/connectfour` (both 1v1, button-driven), `/2048` (solo, button-driven)
- **Licenses** — `/product create|delete|list`, `/license create|delete|list|get|cleardata`, `/getlicense` (self-service), `/apikey create|list|revoke` — see [License System](#license-system) below.

Music and an AI assistant are **not** included — see the conversation for
the scoped roadmap.

## License System

A self-hosted license-key manager for selling scripts/plugins/software, in the same spirit as
commercial "license bot" products but fully under your control: no obfuscation, no hidden
bypass flags, and no telemetry.

- **Products** (`/product`) define a name, an optional Discord role to auto-grant license
  holders, and default IP/HWID caps.
- **Licenses** (`/license create`) generate a random key (unambiguous 32-character charset — no
  `0`/`O`/`1`/`I` mix-ups), DM it to the buyer, optionally grant the product's customer role, and
  support an expiry duration (`30d`, `12h`, …). A background sweep (every 60s) removes expired
  licenses and their granted role automatically — unless the user holds another active license
  that grants the same role.
- **Self-service** (`/getlicense`) lets users view their own keys without needing staff.
- **Rejoin protection** — if a license holder leaves and rejoins the server, their customer
  role(s) are restored automatically for any license that hasn't expired.
- **REST API** (`src/api/server.ts`) lets your sold software verify licenses at runtime over
  HTTP, independent of Discord. Manage keys with `/apikey` (Administrator-only):
  - Each key is scoped to **one guild** — a leaked key can only touch that server's data.
  - Permissions are scoped per key (`licenses:read`, `licenses:create`, `auth`, `*`, etc.) —
    create read-only keys for dashboards and write-scoped keys for backend integrations.
  - Keys are shown **once** at creation and stored only as a SHA-256 hash — the plaintext is
    never persisted, so a database leak doesn't leak usable keys.
  - Per-key rate limiting (requests/minute, configurable per key).

  Example verification call from your software:
  ```bash
  curl -X POST http://localhost:3000/api/auth/verify \
    -H "Authorization: <api-key>" \
    -H "Content-Type: application/json" \
    -d '{"licenseKey":"AB3F-XXXX-XXXX-XXXX","product":"MyPlugin","hwid":"<machine-id>"}'
  ```
  Other endpoints: `GET/POST /api/licenses`, `GET/DELETE /api/licenses/:key`,
  `POST /api/licenses/:key/clear-ip|clear-hwid`, `GET/POST/DELETE /api/products[/:name]`,
  `GET /api/stats`.

## Web Dashboard

A separate Next.js app in `dashboard/` lets server admins manage settings,
tickets, and stats from a browser (Discord OAuth login, reads/writes the
same SQLite file). See `dashboard/README.md` for setup.

## Setup

1. `cd discord-bot && npm install`
2. Copy `.env.example` to `.env` and fill in:
   - `DISCORD_BOT_TOKEN` — from the [Discord Developer Portal](https://discord.com/developers/applications)
   - `DISCORD_CLIENT_ID` — your application's client ID
   - `DISCORD_GUILD_ID` — (optional) a server ID for fast guild-only command registration while testing
3. Invite the bot to your server with the `applications.commands` and `bot` scopes, and at least: Manage Roles, Manage Channels, Kick Members, Ban Members, Moderate Members, Manage Messages.
4. Register slash commands: `npm run deploy-commands`
5. Start the bot: `npm run dev` (auto-reload) or `npm start`

## Notes

- Timeouts use Discord's native timeout feature (max 28 days) instead of a custom mute role.
- The bot needs the **Server Members Intent** and **Message Content Intent** enabled in the Developer Portal (Bot tab) for leveling/automod/welcome features to work.
- Invite tracking and anti-raid/anti-nuke detection also need the **Manage Guild** permission (to read invites and audit logs).
- `/backup restore` never deletes or overwrites existing roles/channels — it only creates ones that are missing by name.
