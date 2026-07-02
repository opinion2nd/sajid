# GeoDash 🟦⚡

**Geometry Dash inside Minecraft.** Auto-run, jump over spikes, die in one hit,
respawn instantly, watch your attempt counter climb — just like the real thing.

> *"POV: Minecraft because the whole group cannot afford Geometry Dash"* — now it's a plugin.

## Supported software

| Bukkit | Spigot | Paper | Folia | Purpur |
|:------:|:------:|:-----:|:-----:|:------:|
| ✅ | ✅ | ✅ | ✅ | ✅ |

**Supported versions:** 1.21, 1.21.2, 1.21.4, 1.21.5, 1.21.8, 1.21.11 (any 1.21.x) — Java 21+.

Folia is supported natively (`folia-supported: true`): every game loop runs on
the owning region's scheduler, teleports use `teleportAsync`, and no global
state is touched from region threads.

## Features

### 🏃 GD-style gameplay (the main focus)
- **Auto-run** — the plugin drives you forward every tick; you only control jumping
- **One-hit death** — touch a spike (pointed dripstone), cactus, magma, lava or fall
  into a gap and you explode instantly
- **Instant respawn** — right back to the start, `Attempt 2`, `Attempt 3`, `Attempt 47`…
- **Progress bar** — boss bar + action bar show your live completion %
- **Jump pads** — slime blocks launch you like GD's yellow pads

### 🏁 Multiplayer race
- `/gd race create <level>` opens a lobby, everyone joins with `/gd race join`
- Synced 3-2-1-GO countdown, first to the finish line wins
- Finish order is announced (`#1`, `#2`, …), winner gets the race reward

### 🛠️ Level editor
- `/gd create <name>` — start a level right where you stand, facing where you look
- Build your course with the `/gd kit` blocks (spikes, jump pads, walls, floor)
- `/gd setfinish <name>` at the end — done, playable
- Tune it with `/gd setspeed` and `/gd setstars`

### 🏆 Leaderboards & stats
- Per level: best %, best time, attempts, clears — stored in `stats.yml`
- `/gd top <level>` — top 10 (finishers by time, then best progress)
- `/gd stats` — your own numbers

### 🎁 Rewards
- Configurable console commands + broadcast on completion (placeholders:
  `%player%`, `%level%`, `%stars%`, `%time%`, `%attempts%`)
- Separate reward for winning a race
- `first-time-only` mode so levels can't be farmed

### ✨ Effects package
- End-rod particle trail while running (configurable)
- Explosion + flame burst on death
- Firework + challenge-complete sound on clearing a level
- Note-block countdown sounds

### 🎮 3 built-in demo levels
Stand somewhere flat and run:

```
/gd demo easy     → 120 blocks, 2★, white/lime
/gd demo medium   → 180 blocks, 5★, gray/orange (adds jump-pad platforms)
/gd demo hard     → 240 blocks, 8★, black/red (double spikes, magma, brutal platforms)
```

The course is generated in front of you and registered instantly — `/gd join demo_easy` and play.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/gd join <level>` | `geodash.play` | Play a level |
| `/gd leave` | `geodash.play` | Quit (inventory/position restored) |
| `/gd list` | `geodash.play` | All levels |
| `/gd top <level>` | `geodash.play` | Leaderboard |
| `/gd stats` | `geodash.play` | Your stats |
| `/gd race join` | `geodash.race` | Join the open race |
| `/gd race create/start/cancel` | `geodash.race.manage` | Run races |
| `/gd create/setstart/setfinish/setspeed/setstars/delete` | `geodash.admin` | Level editor |
| `/gd kit` | `geodash.admin` | Editor block kit |
| `/gd demo <easy\|medium\|hard>` | `geodash.admin` | Generate a demo course |
| `/gd reload` | `geodash.admin` | Reload configs |

Aliases: `/geodash`, `/gdash`.

## Installation

1. Download `GeoDash-1.0.0.jar` (built by CI — see the **geodash-latest** release)
2. Drop it into `plugins/`
3. Restart the server
4. `/gd demo easy` → `/gd join demo_easy` → jump!

## Building from source

```bash
cd GeoDash
mvn package        # → target/GeoDash-1.0.0.jar
```

## Configuration

Everything lives in `config.yml`: run speed, hazard block list, jump-pad boost,
all effect toggles, reward commands and broadcast messages. Levels are stored in
`levels.yml`, player stats in `stats.yml`.
