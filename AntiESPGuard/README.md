# AntiESPGuard

A **server-side** anti-Freecam / anti-ESP plugin for Minecraft
**1.21 – 1.21.11 and 26.1** (Bukkit / Spigot / Paper / Folia / Purpur). It
defeats x-ray-camera cheats by never sending real underground
geometry/entities to players who are on the surface, and reports/kicks known
cheat clients.

The Paper module's `plugin.yml` declares a generic `api-version: '1.21'`,
which Paper only uses as a *minimum* bound — so the same jar loads on every
1.21.x patch and on the new year-based 26.1 line. The actual 26.1 protocol
support comes from whichever **PacketEvents** plugin version the server admin
has installed (PacketEvents >= 2.12.0 added 26.1 support) — our jar only
compiles against PacketEvents' API, it does not bundle it.

This is a clean reimplementation modelled on the `AntiESPFreecam` plugin, with
**one deliberate difference: the Anti-Xray subsystem is intentionally NOT
included.** (The below-Y freecam mask, which is a separate mechanism, *is*
included.)

## Modules

| Module   | Loader / forks                             | How masking is applied                     |
|----------|----------------------------------------------|-------------------------------------------|
| `common` | none (pure Java)                            | shared config model + mask decision logic |
| `paper`  | Paper / Purpur / Folia / Leaf (Bukkit API)  | PacketEvents chunk/entity packet rewriting |

## Feature matrix

| Feature                                              | Paper |
|-------------------------------------------------------|:-----:|
| Below-Y chunk masking (anti-freecam / anti-ESP)        |  ✅   |
| Vanilla-style progressive reveal when descending       |  ✅   |
| Re-mask on returning to surface (`remaskOnReturn`)      |  ✅   |
| Entity masking (anti entity-radar)                      |  ✅   |
| Underground-player masking (combat ESP)                 |  ✅   |
| Per-world / per-dimension enable                         |  ✅   |
| Cheat-mod detection (brand + channels)                   |  ✅   |
| Auto-kick + admin notify + Discord webhook               |  ✅   |
| Anti seed-cracker                                         |  ✅   |
| Update checker                                             |  ✅   |
| `/antiespguard reload\|bypass` command                     |  ✅   |
| Folia support                                               |  ✅   |
| **Anti-Xray**                                                |  ❌   |

## Configuration

`config.yml` lives in `common/src/main/resources/antiespguard/config.yml` and
is copied to `plugins/AntiESPGuard/config.yml` on first run.

Key options: `hideBelowY`, `revealBelowYWhenUnder`, `maskBlock`,
`enabledEnvironments`, `disabledWorlds`, `maskEntities`,
`maskUndergroundPlayers`, `remaskOnReturn`, `modDetection.*`,
`antiSeedCracker.*`, `updateChecker.*`.

## How the masking works

1. A player above `revealBelowYWhenUnder` is a **surface** player.
2. Every chunk packet sent to a surface player has all blocks below
   `hideBelowY` rewritten to `maskBlock` (STONE) — so a freecam camera flown
   underground only ever sees solid stone.
3. When the player descends below the line, chunks reveal exactly like
   vanilla chunk loading: the revealed radius is the player's own view
   distance, refreshed whenever they cross into a new chunk — no extra
   tunables.
4. Entities/players below `hideBelowY` are hidden from surface players
   (entity-radar / combat-ESP defence) without removing them from the tab list.

## Building

Requires JDK 21.

```bash
./gradlew :paper:shadowJar       # -> paper/build/libs/AntiESPGuard-Paper-<ver>.jar
```

The Paper jar requires the **PacketEvents** plugin on the server (declared as a
hard dependency in `plugin.yml`).

## License

MIT.
