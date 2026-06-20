# Brother Craft Bot — Multi-Purpose Discord Bot

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
- **Security suite** — `/security raid|nuke|ghostping|status` (anti-raid join-burst kicking; anti-nuke audit-log-based punishment for mass channel/role create+delete, kicks, bans, webhook creation, permission-overwrite edits, and emoji deletes; ghost-ping detection), `/verify setup|panel` (button-based member verification), `/backup create|list|restore` (additive-only role/channel snapshot restore)
- **AFK** — `/afk`, auto-clears on your next message, replies when someone mentions an AFK user
- **Reminders** — `/remind` (DMs you, falls back to channel ping), persists across restarts
- **Invite tracking** — `/invites`, credits whoever's invite a new member used
- **Snipe** — `/snipe` shows the last deleted message in a channel (5 min TTL)
- **Polls & Suggestions** — `/poll` (up to 5 options, live vote-tally buttons), `/suggestion` (upvote/downvote buttons)
- **Custom embeds** — `/embed` for staff-sent announcements
- **General** — `/ping`, `/botinfo`, `/serverinfo`, `/userinfo`, `/avatar`
- **Fun** — `/8ball`, `/coinflip`, `/roll`, `/rps`, `/meme`, `/joke`, `/fact`, `/catfact`, `/hug`, `/kiss`, `/slap`, `/dance`, `/compliment`, `/roast`, `/ship` (gif-based commands use the free, keyless [otakugifs.xyz](https://otakugifs.xyz) API; meme/joke/fact commands use other free keyless APIs)
- **Games** — `/tictactoe`, `/connectfour` (both 1v1, button-driven), `/2048` (solo, button-driven)

Music and an AI assistant are **not** included — see the conversation for
the scoped roadmap.

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

## Deploying on a Pterodactyl panel (e.g. NyctoHost)

If your hosting uses a Pterodactyl panel, request a **Node.js egg/server**
(this bot is Node.js/TypeScript, not Python).

Some Node.js eggs let you set a free-form **Startup Command** — if so, set it
to `bash pterodactyl-start.sh` (see that file for what it does).

Many panels' Node.js eggs are fixed instead (they only run
`node <Main File>` and offer a "Node Packages" field, with no shell access).
For those, use `launcher.js`:

1. Upload/clone this `discord-bot/` folder as the server's contents.
2. Create `.env` (bot) and `dashboard/.env.local` (dashboard) from their
   `.env.example` files and fill in the values (see Setup above and
   `dashboard/README.md`).
3. Under **Startup**, set the **Main File** variable to `discord-bot/launcher.js`
   (or `launcher.js` if you uploaded this folder's contents directly to the
   container root), and clear/empty the **Node Packages** field.
4. Start the server. `launcher.js` installs dependencies if missing, registers
   slash commands, runs the bot, builds the dashboard, and serves it on the
   panel's allocated port (`$SERVER_PORT`) — open
   `http://<your-panel-address>:<port>` in a browser once it's up.

## Notes

- Timeouts use Discord's native timeout feature (max 28 days) instead of a custom mute role.
- The bot needs the **Server Members Intent** and **Message Content Intent** enabled in the Developer Portal (Bot tab) for leveling/automod/welcome features to work.
- Invite tracking and anti-raid/anti-nuke detection also need the **Manage Guild** permission (to read invites and audit logs).
- `/backup restore` never deletes or overwrites existing roles/channels — it only creates ones that are missing by name.
