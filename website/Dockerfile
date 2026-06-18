# Production image for the Brother Craft web app.
# Build context = the `website/` directory.

FROM node:20-slim AS base
RUN apt-get update && apt-get install -y openssl ca-certificates && rm -rf /var/lib/apt/lists/*
# Pin pnpm to match packageManager so corepack doesn't need to download at build.
RUN corepack enable && corepack prepare pnpm@10.33.0 --activate
ENV NEXT_TELEMETRY_DISABLED=1
WORKDIR /app

# ── Build ─────────────────────────────────────────────
FROM base AS builder
COPY . .
RUN pnpm install --frozen-lockfile
RUN pnpm --filter @brothercraft/db generate
RUN pnpm --filter @brothercraft/web build

# ── Runtime ───────────────────────────────────────────
FROM base AS runner
ENV NODE_ENV=production
ENV PORT=3000

# Copy the whole built workspace (incl. node_modules with the Prisma engine).
COPY --from=builder /app ./

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 3000
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["pnpm", "--filter", "@brothercraft/web", "start"]
