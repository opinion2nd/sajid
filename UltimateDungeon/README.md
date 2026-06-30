# UltimateDungeon

A commercial-grade, fully procedural dungeon plugin for Paper-based Minecraft
servers (1.21.x). Solo and party runs, eight multi-phase bosses, ten monster
types, eight traps, five puzzles, five themes, a six-tier loot system, isolated
dungeon worlds, and a complete reward, statistics and GUI suite — all driven by
configuration.

## Requirements

- **Server:** Paper 1.21.x (Purpur, Pufferfish and Spigot also supported;
  Folia is detected and degrades gracefully).
- **Java:** 21+
- **Optional:** [Vault](https://www.spigotmc.org/resources/vault.34315/) for
  money rewards (a no-op economy is used when Vault is absent).

## Installation

1. Download `UltimateDungeon-<version>.jar` from the build artifacts / release.
2. Drop it into your server's `plugins/` folder.
3. Start the server once to generate the configuration files, then stop, tune,
   and start again.

## Commands

### `/dungeon` (aliases: `/ud`, `/dungeons`)

| Sub-command | Description | Permission |
|-------------|-------------|------------|
| *(none)* | Open the main menu GUI | `dungeon.use` |
| `solo [theme] [difficulty]` | Start a solo dungeon | `dungeon.use` |
| `party [theme] [difficulty]` | Start a dungeon for your party (leader only) | `dungeon.use` |
| `leave` | Leave your current dungeon | `dungeon.use` |
| `stats` | View your personal statistics | `dungeon.stats` |
| `reload` | Reload all configuration files | `dungeon.reload` |
| `admin info` | Show active instance count | `dungeon.admin` |

### `/party` (aliases: `/dparty`, `/dp`)

`create`, `invite <player>`, `accept`, `deny`, `leave`, `kick <player>`,
`transfer <player>`, `disband`, `list`, `chat <message>` — see in-game help.

## Permissions

| Node | Default | Grants |
|------|---------|--------|
| `dungeon.*` | op | Everything |
| `dungeon.use` | true | Run and join dungeons |
| `dungeon.party` | true | Create and manage parties |
| `dungeon.stats` | true | View statistics |
| `dungeon.admin` | op | Administrative controls |
| `dungeon.reload` | op | Reload configuration |

## Configuration

All gameplay values live in configuration — nothing is hard-coded.

| File | Controls |
|------|----------|
| `config.yml` | Debug, language, instance prefix |
| `messages.yml` | All player-facing text (MiniMessage) |
| `dungeon.yml` | Size, room weights, corridor length, spawn/decoration rates |
| `themes.yml` | Per-theme palette, ambience, monster/boss pools |
| `bosses.yml` | Per-boss health, phases, five abilities, BossBar, drops, dialogue |
| `monsters.yml` | Per-monster stats, abilities, equipment, drops, spawn weight |
| `traps.yml` | Per-trap damage, cooldown, trigger, radius, effects |
| `rewards.yml` | Per-event money, XP, loot table, commands, tokens |
| `loot.yml` | Rarity chances and per-table item pools |
| `gui.yml` | GUI layouts |
| `party.yml` | Party size, timeouts, friendly fire, late join |
| `difficulty.yml` | Per-difficulty multipliers and loot tier bonus |
| `database.yml` | SQLite/MySQL connection and pool settings |
| `performance.yml` | Entity caps, cache TTLs, async/expiry settings |

## Architecture highlights

- **Isolated worlds** — dungeons generate in a dedicated void world; survival
  worlds are never touched.
- **Async generation** — layout planning runs off the main thread; only block
  placement is synchronous.
- **Difficulty scaling** — one resolver feeds monster, boss, trap and loot
  scaling.
- **Per-player reward isolation** — every player rolls their own loot; rewards
  cannot be contested or stolen.
- **Clean teardown** — every dungeon end despawns its entities and releases
  state; the database closes cleanly on shutdown.

## Building from source

```bash
cd UltimateDungeon
./gradlew shadowJar
# → build/libs/UltimateDungeon-<version>.jar
```
