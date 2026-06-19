#!/data/data/com.termux/files/usr/bin/bash
# Start Brother Craft on Termux. Run from the `website/` folder.
set -e

PGDATA="$PREFIX/var/lib/postgresql"

# Make sure Postgres is running.
pg_ctl -D "$PGDATA" -l "$PREFIX/var/lib/postgresql/log" start 2>/dev/null || true
sleep 2

echo "==> Starting Brother Craft on http://localhost:3000"
echo "    (first start in dev mode compiles on demand — give it a moment)"
cd apps/web
exec pnpm dev
