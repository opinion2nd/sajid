#!/bin/bash
# Brother Craft setup INSIDE a proot-distro Debian (Termux).
# Prisma needs a glibc Linux, which Debian-in-Termux provides.
# Run from the project root:  bash scripts/debian-setup.sh
set -e

echo "==> Installing system packages..."
apt-get update
apt-get install -y curl unzip git ca-certificates postgresql postgresql-contrib procps

if ! command -v node >/dev/null 2>&1; then
  echo "==> Installing Node.js 20..."
  curl -fsSL https://deb.nodesource.com/setup_20.x | bash -
  apt-get install -y nodejs
fi
command -v pnpm >/dev/null 2>&1 || npm install -g pnpm

echo "==> Starting PostgreSQL..."
service postgresql start || pg_ctlcluster "$(ls /etc/postgresql)" main start || true
sleep 3

echo "==> Creating database + role..."
su postgres -c "psql -tc \"SELECT 1 FROM pg_roles WHERE rolname='brothercraft'\" | grep -q 1 || psql -c \"CREATE ROLE brothercraft LOGIN PASSWORD 'brothercraft' SUPERUSER;\""
su postgres -c "psql -tc \"SELECT 1 FROM pg_database WHERE datname='brothercraft'\" | grep -q 1 || createdb -O brothercraft brothercraft"

echo "==> Writing .env..."
[ -f .env ] || cp .env.example .env
sed -i 's#^DATABASE_URL=.*#DATABASE_URL=postgresql://brothercraft:brothercraft@localhost:5432/brothercraft?schema=public#' .env
sed -i 's#^AUTH_SECRET=.*#AUTH_SECRET=brothercraft-debian-secret-change-me-1234#' .env
cp .env apps/web/.env 2>/dev/null || true
cp .env packages/db/.env 2>/dev/null || true

echo "==> Installing dependencies (a few minutes)..."
pnpm install
echo "==> Setting up the database..."
pnpm --filter @brothercraft/db generate
pnpm --filter @brothercraft/db exec prisma migrate deploy
pnpm --filter @brothercraft/db seed || true

echo ""
echo "============================================"
echo " Done! Start the site with:"
echo "   bash scripts/debian-run.sh"
echo " Then open http://localhost:3000"
echo "============================================"
