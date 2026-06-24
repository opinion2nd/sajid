# Flaming License Bot

A standalone, self-contained Discord bot for selling and managing software license keys —
everything is done with Discord **slash commands**, nothing requires editing files after
the initial setup.

## Why this exists

This is the license-management module pulled out of the larger Flaming multi-purpose bot
into its own lightweight project, with one goal: **run anywhere with zero friction.**

- **Zero native dependencies.** Only `discord.js` and `dotenv` — no `better-sqlite3`,
  no `node-gyp`, no compiled bindings. That means it installs and runs identically on:
  - Your **PC** (Windows/macOS/Linux)
  - Your **phone** via [Termux](https://termux.dev/) (Android)
  - Any **free/cheap hosting** (Railway, Render, a VPS, etc.) — even ones without build tools
- **All data lives in one JSON file** (`data/license-bot.json`), written atomically so a
  crash mid-write can't corrupt it. No database server to install or configure.
- **Everything is a slash command.** Webhook URL, products, licenses, API keys — all
  configured live in Discord. The only thing you ever touch in `.env` is your bot token.

## Commands

| Command | Who | What it does |
|---|---|---|
| `/product create\|delete\|list` | Manage Server | Define products, their customer role, and default IP/HWID caps |
| `/license create` | Manage Server | Issue a license directly to a Discord user (DMs them the key) |
| `/license generate` | Manage Server | Bulk-generate unclaimed **stock keys** for marketplace sales — returns a `.txt` of the keys |
| `/license delete\|list\|get\|cleardata` | Manage Server | Manage existing licenses |
| `/redeem <key>` | Everyone | Claim a stock key you bought elsewhere — grants the product's role and starts its expiry timer |
| `/getlicense` | Everyone | View your own license keys |
| `/apikey create\|list\|revoke` | Administrator | Manage REST API keys for your sold software to verify licenses |
| `/config webhook\|view` | Administrator | Set/view the audit-log webhook for license events — no `.env` edits needed |
| `/help` | Everyone | List all commands |

### The stock-key + redeem flow

For selling on a marketplace (BuiltByBit, your own storefront, etc.) where you don't know
the buyer's Discord account at sale time:

1. `/license generate product:MyPlugin count:50 expires_after_redeem:30d` — pre-generates
   50 keys, none tied to anyone yet. The expiry timer doesn't start until redeemed.
2. Hand a key to each buyer (e.g. include it in their download).
3. The buyer runs `/redeem <key>` in your server — the key binds to their account, their
   customer role is granted, and the 30-day timer starts from that moment.

## Setup

### 1. Create the Discord application

1. Go to the [Discord Developer Portal](https://discord.com/developers/applications) and
   create an application.
2. Under **Bot**, create a bot and copy the token.
3. Under **OAuth2 → URL Generator**, select scopes `bot` and `applications.commands`, and
   bot permission **Manage Roles** (to grant/revoke customer roles). Use the generated URL
   to invite the bot to your server.

### 2. Install and configure

```bash
npm install
cp .env.example .env
```

Edit `.env`:
- `DISCORD_BOT_TOKEN` — your bot's token
- `DISCORD_CLIENT_ID` — your application's client ID
- `DISCORD_GUILD_ID` — (optional) a server ID for instant command registration while testing
- `LICENSE_API_PORT` — port for the verification REST API (most hosts override this with `PORT` automatically)

### 3. Register and run

```bash
npm run deploy-commands
npm run dev     # auto-reload while developing
npm start       # production
```

## Running on your phone (Termux)

```bash
pkg install nodejs git
git clone <your-repo-url>
cd license-bot
npm install
cp .env.example .env && nano .env   # fill in your token
npm run deploy-commands
npm start
```

No build step is needed because there's nothing native to compile — this is the entire
reason the JSON-file store was chosen over a SQLite-based one.

## Running on free/cheap hosting (Railway, Render, a VPS, etc.)

1. Push this folder to a Git repo and connect it to your host.
2. Set the start command to `npm start` (and a build command of `npm install` if your
   host doesn't do that automatically).
3. Set the environment variables (`DISCORD_BOT_TOKEN`, `DISCORD_CLIENT_ID`) in the host's
   dashboard — never commit `.env`.
4. The REST API automatically listens on the host's `PORT` env var if set (falls back to
   `LICENSE_API_PORT`, default `3000`), and responds to `GET /` with a 200 health check so
   the host's liveness probe doesn't fail.
5. **Important:** `data/license-bot.json` must live on **persistent** storage. Most hosts
   wipe the filesystem on redeploy unless you attach a volume/disk — check your host's docs
   for "persistent disk" or "volume" and mount it at the bot's working directory.

## REST API (for your sold software)

Independent of Discord, your sold software can verify a license over HTTP. Create a
scoped key with `/apikey create` first.

```bash
curl -X POST http://localhost:3000/api/auth/verify \
  -H "Authorization: <api-key>" \
  -H "Content-Type: application/json" \
  -d '{"licenseKey":"AB3F-XXXX-XXXX-XXXX","product":"MyPlugin","hwid":"<machine-id>"}'
```

- Each key is scoped to **one guild** — a leaked key can only touch that server's data.
- Permissions are scoped per key (`licenses:read`, `licenses:create`, `auth`, `*`, etc.).
- Keys are shown **once** at creation and stored only as a SHA-256 hash.
- Per-key rate limiting (requests/minute, configurable per key).

Other endpoints: `GET/POST /api/licenses`, `GET/DELETE /api/licenses/:key`,
`POST /api/licenses/:key/clear-ip|clear-hwid`, `GET/POST/DELETE /api/products[/:name]`,
`GET /api/stats`.

## Notes

- A background sweep (every 60s) removes expired licenses and their granted role
  automatically — unless the user holds another active license that grants the same role.
- If a license holder leaves and rejoins the server, their customer role(s) are restored
  automatically for any license that hasn't expired.
- `data/` and `.env` are git-ignored — back up `data/license-bot.json` yourself if you
  care about not losing your license database.
