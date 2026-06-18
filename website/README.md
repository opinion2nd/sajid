# Brother Craft

A multi-vendor Minecraft marketplace — sellers list plugins, configs, builds and
services; buyers purchase with **bKash / Nagad**; purchases mint **license keys**
that Minecraft plugins validate against the marketplace API. Includes a Discord
bot and a background worker.

> Built as a pnpm + Turborepo monorepo. Lives beside the existing plugins in this
> repository. The license system is ported and generalized from
> `anti-freecam/license-server`, preserving the `/api/v1/license/validate` wire
> contract so existing plugin clients keep working unchanged.

## Layout

```
apps/
  web/        Next.js 15 (App Router) — frontend + API route handlers
  bot/        discord.js worker            (Phase 3)
  worker/     BullMQ jobs                   (Phase 2)
packages/
  db/             Shared Prisma schema + client (Postgres)
  license-core/   Keygen + validation state machine (ported from anti-freecam)
  payments/       PaymentProvider interface + SSLCommerz impl (Phase 1)
  auth/           Auth.js config                                (Phase 1)
  sdk/            Seller plugin SDK (TS + Java reference)        (Phase 3)
```

## Quick start

```bash
# 1. Bring up Postgres + Redis
docker compose up -d

# 2. Install deps
pnpm install

# 3. Configure env
cp .env.example .env   # then fill in secrets

# 4. Generate Prisma client + run migrations
pnpm db:generate
pnpm db:migrate

# 5. Run the web app
pnpm dev
```

- App: http://localhost:3000
- Health: http://localhost:3000/api/health
- License API: `POST http://localhost:3000/api/v1/license/validate`

## License API contract (unchanged from anti-freecam)

```http
POST /api/v1/license/validate
{ "key": "BC-XXXX-XXXX-XXXX-XXXX", "serverId": "<uuid>", "pluginVersion": "1.0.0" }

200 OK
{ "status": "VALID|INVALID|MISMATCH|EXPIRED", "message": "...",
  "expiresAt": "ISO|null", "boundServerId": "<uuid>|null" }
```

## Roadmap

See the planning document for the phased roadmap (Phase 0 foundation → Phase 4
growth). Current status: **Phase 0 — Foundation** scaffolded.
