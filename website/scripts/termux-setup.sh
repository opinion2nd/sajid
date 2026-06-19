#!/data/data/com.termux/files/usr/bin/bash
# Brother Craft — one-shot Termux setup.
# Run from inside the `website/` folder:  bash scripts/termux-setup.sh
set -e

echo "==> Brother Craft Termux setup"

# 1. System packages
echo "==> Installing packages (nodejs, postgresql)..."
pkg update -y
pkg install -y nodejs postgresql

# 2. pnpm
if ! command -v pnpm >/dev/null 2>&1; then
  echo "==> Installing pnpm..."
  npm install -g pnpm
fi

# 3. PostgreSQL data dir + start
PGDATA="$PREFIX/var/lib/postgresql"
if [ ! -d "$PGDATA/base" ]; then
  echo "==> Initialising PostgreSQL..."
  mkdir -p "$PGDATA"
  initdb "$PGDATA"
fi
echo "==> Starting PostgreSQL..."
pg_ctl -D "$PGDATA" -l "$PREFIX/var/lib/postgresql/log" start || true
sleep 3

# 4. Database + role
echo "==> Creating database and role..."
psql -d postgres -tc "SELECT 1 FROM pg_roles WHERE rolname='brothercraft'" | grep -q 1 || \
  psql -d postgres -c "CREATE ROLE brothercraft LOGIN PASSWORD 'brothercraft' SUPERUSER;"
psql -d postgres -tc "SELECT 1 FROM pg_database WHERE datname='brothercraft'" | grep -q 1 || \
  createdb -O brothercraft brothercraft

# 5. .env
if [ ! -f .env ]; then
  echo "==> Creating .env..."
  cp .env.example .env
  # localhost Postgres, demo mode
  sed -i 's#^DATABASE_URL=.*#DATABASE_URL=postgresql://brothercraft:brothercraft@localhost:5432/brothercraft?schema=public#' .env
  sed -i 's#^AUTH_SECRET=.*#AUTH_SECRET=brothercraft-termux-secret-change-me-please-1234#' .env
fi
cp .env apps/web/.env 2>/dev/null || true
cp .env packages/db/.env 2>/dev/null || true

# 6. Install deps + prisma + migrate + seed
echo "==> Installing dependencies (this can take a few minutes)..."
pnpm install
echo "==> Setting up the database..."
pnpm --filter @brothercraft/db generate
pnpm --filter @brothercraft/db migrate:deploy || pnpm --filter @brothercraft/db exec prisma migrate dev --name init
pnpm --filter @brothercraft/db seed || true

echo ""
echo "============================================"
echo " Setup done! Now start the site with:"
echo "   bash scripts/termux-run.sh"
echo " Then open http://localhost:3000 in your browser."
echo "============================================"
