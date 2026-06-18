#!/bin/sh
set -e

# Apply database migrations before starting (safe to run repeatedly).
if [ -n "$DATABASE_URL" ]; then
  echo "Running database migrations..."
  npx --no-install prisma migrate deploy --schema packages/db/prisma/schema.prisma || \
    echo "migrate deploy skipped/failed — continuing"
fi

exec "$@"
