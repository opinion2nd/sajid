# Defensive-feature tester bot

A small, separate Discord bot for exercising the main bot's anti-nuke,
automod, and ghost-ping detection on a server you own, using a single
slash command instead of a CLI script. Available in both Node.js
(discord.js) and Python (discord.py) — pick one, they're equivalent.

**Use a different bot account from your main bot** — the main bot is
exempt from its own anti-nuke punishment, so it can't usefully test
itself.

Every test fires its creates/deletes/sends concurrently instead of
looping with sleeps, so each run is as fast as Discord's API allows —
the only floor left is real network latency and rate limits.

## Setup (common to both versions)

1. Create a second application + bot at the
   [Discord Developer Portal](https://discord.com/developers/applications).
2. In the **Bot** tab, enable **Server Members Intent** (needed for the
   `automod-mentions`, `ghostping`, `nuke-kicks`, and `nuke-bans` tests,
   which act on real members).
3. Invite it to your server (OAuth2 → URL Generator) with scope `bot` and
   permissions: Manage Channels, Manage Roles, Manage Webhooks, Manage
   Emojis and Stickers, Kick Members, Ban Members, Send Messages.
4. Copy `.env.example` to `.env` and fill in `TEST_BOT_TOKEN`,
   `TEST_GUILD_ID`, and `TEST_OWNER_ID` (your own Discord user ID —
   `/test` only responds to this user, even though the command is
   visible to Administrators in the UI). `TEST_CLIENT_ID` is only needed
   for the Node.js version.

### Node.js (discord.js)

5. `npm install`
6. `npm run deploy-commands`
7. `npm start`

### Python (discord.py)

5. `pip install -r requirements.txt`
6. `python test_bot.py` — commands sync to `TEST_GUILD_ID` automatically
   on startup, no separate deploy step needed.

## Usage

In Discord, type `/test` and pick a subcommand:

- `/test nuke-channels` — creates + rapidly deletes 4 channels (tests `/security nuke`)
- `/test nuke-roles` — creates + rapidly deletes 4 roles (tests `/security nuke`)
- `/test nuke-webhooks channel:#general` — creates + rapidly deletes 4 webhooks
- `/test nuke-permissions` — creates temp channels and rapidly locks `@everyone` out of each, then cleans them up
- `/test nuke-emojis` — creates + rapidly deletes 4 emojis (using the bot's own avatar as the image)
- `/test nuke-kicks target1:@user [target2] [target3] [target4]` — rapidly kicks the given members. **Actually kicks real accounts** — only target people who've agreed to it (e.g. your own alt accounts), since kicks can't be undone and they'll need a fresh invite to rejoin.
- `/test nuke-bans target1:@user [target2] [target3] [target4]` — rapidly bans then immediately unbans the given members. **Actually bans real accounts** (briefly) — only target consenting people; they'll still need a fresh invite link to rejoin after the unban.
- `/test automod-invite channel:#general` — sends a fake invite link
- `/test automod-caps channel:#general` — sends an ALL-CAPS message
- `/test automod-spam channel:#general` — sends the same message 4x quickly
- `/test automod-mentions channel:#general` — pings several real members (actually pings people)
- `/test ghostping channel:#general` — pings a member then deletes the message fast (actually pings someone)

Run these on a test server, or warn members first — the mention/ghostping/
kick/ban tests act on real people. Check your main bot's modlog channel
(`/config modlog`) for the results of each test.

## What the main bot's anti-nuke actually catches

As of this version, Brother Craft's anti-nuke (`/security nuke`) watches
for burst activity (default: 3+ actions in 30s by the same person) across:
channel create/delete, role create/delete, mass bans, mass kicks, webhook
creation, permission-overwrite edits (channel lockdowns), and emoji
deletes. `nuke-channels` through `nuke-bans` above each exercise one or
more of these.
