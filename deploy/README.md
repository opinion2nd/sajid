# Hosting the license system

Two pieces, both Node + SQLite, no Postgres required:

- `anti-freecam/license-server` — the API that issues/validates/revokes keys.
- `license-bot` — the Discord bot admins use to run `/license issue|revoke|unbind|info|list`.

## Option A: PC / VPS with PM2

```bash
cd anti-freecam/license-server
npm install
cp .env.example .env   # fill in ADMIN_SECRET
npx prisma migrate deploy
npm run build

cd ../../license-bot
npm install
cp .env.example .env   # fill in DISCORD_*, LICENSE_SERVER_URL, LICENSE_ADMIN_SECRET (same value as ADMIN_SECRET above)
npm run deploy-commands   # registers the /license slash command

npm install -g pm2
pm2 start "node dist/index.js" --name license-server --cwd anti-freecam/license-server
pm2 start "npx tsx src/index.ts" --name license-bot --cwd license-bot
pm2 save
pm2 startup   # follow the printed instructions to survive reboots
```

## Option B: Docker Compose

```bash
cd deploy
cat > .env <<'EOF'
ADMIN_SECRET=change-me-to-a-long-random-secret
DISCORD_BOT_TOKEN=...
DISCORD_CLIENT_ID=...
DISCORD_GUILD_ID=...
LICENSE_BOT_ADMIN_USER_IDS=...
LICENSE_BOT_ADMIN_ROLE_IDS=...
EOF

docker compose up -d --build
```

The SQLite file lives in the named volume `license-db`, so `docker compose restart` /
`docker compose up -d` (rebuild without `down -v`) won't lose existing licenses or their
server bindings. Only `docker compose down -v` deletes the volume — avoid that in
production.

If deploying to a free-tier PaaS (Render/Railway/etc.) instead of a VPS, make sure the
disk backing `/app/data` is a **persistent volume**, not the platform's default ephemeral
filesystem — an ephemeral disk gets wiped on every redeploy, which would silently unbind
every issued license.

## Option C: Termux (mobile)

`license-bot` has zero native dependencies (pure discord.js + fetch) and runs fine under
Termux via `npm install && npx tsx src/index.ts`.

`license-server` depends on Prisma's native query engine binary, which is less predictable
on Termux's architecture/libc. If it gives you trouble, run `license-server` on a PC/VPS
instead and point `license-bot`'s `LICENSE_SERVER_URL` (and any plugin's `license.api-url`)
at that server — only `license-bot` needs to run on the phone.

## Env vars reference

| Var | Used by |
|---|---|
| `DATABASE_URL`, `ADMIN_SECRET`, `PORT`, `HOST` | license-server |
| `DISCORD_BOT_TOKEN`, `DISCORD_CLIENT_ID`, `DISCORD_GUILD_ID`, `LICENSE_SERVER_URL`, `LICENSE_ADMIN_SECRET`, `LICENSE_BOT_ADMIN_USER_IDS`, `LICENSE_BOT_ADMIN_ROLE_IDS` | license-bot |
| `license.key`, `license.api-url` (config.yml / antifreecam.properties) | Minecraft plugins |
| `LICENSE_API_URL`, `LICENSE_KEY`, `SERVER_ID` (optional) | sold Discord bots, via `discord-bot-license-client/licenseClient.ts` |
