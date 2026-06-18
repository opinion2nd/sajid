# Deploying Brother Craft

Three ways to put Brother Craft online, easiest first. The app runs in **demo
mode** with no third-party accounts, so you can deploy now and add real
bKash/Nagad, Discord and cloud storage later.

---

## Option 1 — Railway (easiest, no Docker knowledge needed)

1. Push this repo to GitHub (already done on your branch).
2. Go to <https://railway.app> → **New Project → Deploy from GitHub repo** →
   pick `opinion2nd/sajid`.
3. Set the service **Root Directory** to `website`.
4. Add a **PostgreSQL** plugin (Railway → New → Database → PostgreSQL). It
   creates a `DATABASE_URL` automatically — reference it in your web service.
5. Add environment variables (Service → Variables):
   ```
   AUTH_SECRET=<run: openssl rand -base64 32>
   APP_URL=https://<your-railway-domain>
   PAYMENT_PROVIDER=mock
   STORAGE_DRIVER=local
   PLATFORM_BKASH_NUMBER=01811799277
   PLATFORM_NAGAD_NUMBER=01811799277
   ```
6. Set the **Start Command**:
   ```
   pnpm --filter @brothercraft/db migrate:deploy && pnpm --filter @brothercraft/web start
   ```
   and **Build Command**:
   ```
   pnpm install && pnpm --filter @brothercraft/db generate && pnpm --filter @brothercraft/web build
   ```
7. Deploy. Open the generated URL → your site is live. Seed demo data once from
   the Railway shell: `pnpm --filter @brothercraft/db seed`.

> Render.com works the same way (Blueprint / Web Service + PostgreSQL add-on).

---

## Option 2 — Any VPS with Docker (full control)

On a server (DigitalOcean / Hetzner / AWS Lightsail, ideally Singapore/Mumbai
for a Bangladesh audience):

```bash
git clone <your-repo> && cd sajid/website
cp .env.example .env          # edit: AUTH_SECRET, APP_URL, POSTGRES_PASSWORD, etc.
docker compose -f docker-compose.prod.yml up -d --build
docker compose -f docker-compose.prod.yml exec web \
  npx prisma db seed --schema packages/db/prisma/schema.prisma   # optional demo data
```

Put **Cloudflare** (free) in front for HTTPS + CDN, pointing your domain at the
server. The web container runs migrations automatically on start.

---

## Option 3 — Vercel (web) + managed Postgres

1. Import the repo on <https://vercel.com>, set **Root Directory** = `website/apps/web`.
2. Add a managed Postgres (Neon / Supabase / Vercel Postgres) and set `DATABASE_URL`.
3. Add the same env vars as Option 1.
4. Run `prisma migrate deploy` once (Neon/Supabase SQL console or a local run
   against the prod `DATABASE_URL`).

The Discord bot (`apps/bot`) is a long-running process — run it on Railway/VPS,
not Vercel. Start it with `pnpm --filter @brothercraft/bot start` after setting
`DISCORD_BOT_TOKEN` and registering commands (`pnpm --filter @brothercraft/bot register`).

---

## Going from demo → real

| Feature | Env to set |
| --- | --- |
| bKash/Nagad payments | `PAYMENT_PROVIDER=sslcommerz`, `SSLCOMMERZ_STORE_ID`, `SSLCOMMERZ_STORE_PASSWORD`, `SSLCOMMERZ_SANDBOX=false` |
| Cloud file storage | `STORAGE_DRIVER=s3`, `STORAGE_ENDPOINT`, `STORAGE_BUCKET`, `STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY` |
| Email receipts | `EMAIL_API_KEY` (Resend), `EMAIL_FROM` |
| Discord login + bot | `AUTH_DISCORD_ID`, `AUTH_DISCORD_SECRET`, `DISCORD_BOT_TOKEN`, `DISCORD_CLIENT_ID`, `DISCORD_GUILD_ID`, `DISCORD_CUSTOMER_ROLE_ID` |
| Your 10% payout number | `PLATFORM_BKASH_NUMBER`, `PLATFORM_NAGAD_NUMBER` (default `01811799277`) |
