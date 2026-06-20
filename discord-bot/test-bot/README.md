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
   `automod-mentions` and `ghostping` tests, which mention real members).
3. Invite it to your server (OAuth2 → URL Generator) with scope `bot` and
   permissions: Manage Channels, Manage Roles, Send Messages.
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
- `/test automod-invite channel:#general` — sends a fake invite link
- `/test automod-caps channel:#general` — sends an ALL-CAPS message
- `/test automod-spam channel:#general` — sends the same message 4x quickly
- `/test automod-mentions channel:#general` — pings several real members (actually pings people)
- `/test ghostping channel:#general` — pings a member then deletes the message fast (actually pings someone)

Run these on a test server, or warn members first — the mention/ghostping
tests notify real people. Check your main bot's modlog channel
(`/config modlog`) for the results of each test.
