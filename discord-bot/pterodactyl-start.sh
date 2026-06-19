#!/bin/bash
# Startup script for Pterodactyl-based hosts (e.g. NyctoHost) running a Node.js egg.
# Set this repo's root (discord-bot/) as the server's working directory, then set
# the Startup Command in the panel to:
#   bash pterodactyl-start.sh
#
# Required environment variables (set under Startup > Variables, or a .env file
# in this directory): DISCORD_BOT_TOKEN, DISCORD_CLIENT_ID, DISCORD_CLIENT_SECRET,
# DISCORD_REDIRECT_URI, SESSION_SECRET. SERVER_PORT is provided automatically by
# the panel and is used for the dashboard's web port.
set -e
cd "$(dirname "$0")"

if [ ! -d node_modules ]; then
  npm install
fi
if [ ! -d dashboard/node_modules ]; then
  (cd dashboard && npm install)
fi

npm run deploy-commands

# Bot runs in the background; the dashboard stays in the foreground so the
# panel's process monitor (and the allocated port) track the web server.
npm start &
BOT_PID=$!
trap "kill $BOT_PID" EXIT

export BOT_DB_PATH=../data/bot.sqlite3
export DISCORD_REDIRECT_URI="${DISCORD_REDIRECT_URI:-http://${SERVER_ADDR:-localhost}:${SERVER_PORT:-3000}/api/auth/callback}"

cd dashboard
if [ ! -d .next ]; then
  npm run build
fi
npx next start -p "${SERVER_PORT:-3000}" -H 0.0.0.0
