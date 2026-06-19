#!/bin/bash
# Start Brother Craft inside proot-distro Debian. Run from the project root.
set -e
service postgresql start 2>/dev/null || pg_ctlcluster "$(ls /etc/postgresql)" main start 2>/dev/null || true
sleep 2
echo "==> Starting Brother Craft on http://localhost:3000"
cd apps/web
exec pnpm dev
