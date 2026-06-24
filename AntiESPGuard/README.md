# AntiESPGuard

A multi-platform, **server-side** anti-Freecam / anti-ESP system for Minecraft
**1.21.x**. It defeats x-ray-camera cheats by never sending real underground
geometry/entities to players who are on the surface, and reports/kicks known
cheat clients.

This is a clean reimplementation modelled on the `AntiESPFreecam` plugin, with
**one deliberate difference: the Anti-Xray subsystem is intentionally NOT
included.** (The below-Y freecam mask, which is a separate mechanism, *is*
included.)

> ⚠️ **Build / test status.** Only the `common` module was compiled in the
> environment this was authored in — the organisation's network policy blocks
> the Paper, PacketEvents, Fabric and NeoForge maven repositories, so the
> platform modules could not be compiled or run here. The **Paper** module
> closely follows already-proven PacketEvents code that lives in this repo
> (`/AntiFreecam`). The **Fabric** and **NeoForge** modules are complete,
> idiomatic implementations but **must be compiled and tested on a real server**
> before production use (Mixin targets in particular should be verified against
> your exact mappings).

## Modules

| Module      | Loader / forks                              | How masking is applied                          |
|-------------|---------------------------------------------|-------------------------------------------------|
| `common`    | none (pure Java)                            | shared config model + mask decision logic       |
| `paper`     | Paper / Purpur / Folia / Leaf (Bukkit API)  | PacketEvents chunk/entity packet rewriting      |
| `fabric`    | Fabric (dedicated server)                   | Mixin into the outgoing packet `send` path      |
| `neoforge`  | NeoForge (dedicated server)                 | Mixin into the outgoing packet `send` path      |

> **Note on Fabric.** The reference plugin is a Bukkit plugin and does **not**
> run on Fabric — Fabric is a separate ecosystem. This project adds genuine
> Fabric and NeoForge server mods so the feature set is available there too.

## Feature matrix

| Feature                                            | Paper | Fabric | NeoForge |
|----------------------------------------------------|:-----:|:------:|:--------:|
| Below-Y chunk masking (anti-freecam / anti-ESP)    |  ✅   |   ✅   |    ✅    |
| Progressive reveal when descending (`lazyUnmask`)  |  ✅   |   ✅   |    ✅    |
| Re-mask on returning to surface (`remaskOnReturn`) |  ✅   |   ✅   |    ✅    |
| Entity masking (anti entity-radar)                 |  ✅   |   ✅   |    ✅    |
| Underground-player masking (combat ESP)            |  ✅   |   ✅   |    ✅    |
| Per-world / per-dimension enable                   |  ✅   |   ✅   |    ✅    |
| Cheat-mod detection (brand + channels)             |  ✅   |   ✅   |    ✅    |
| Auto-kick + admin notify + Discord webhook         |  ✅   |   ✅   |    ✅    |
| Anti seed-cracker                                  |  ✅   |   —¹   |    —¹    |
| Update checker                                     |  ✅   |   —²   |    —²    |
| `/antiespguard reload\|bypass` command             |  ✅   |   —³   |    —³    |
| Folia support                                      |  ✅   |  n/a   |   n/a    |
| **Anti-Xray**                                      |  ❌   |   ❌   |    ❌    |

¹ The hashed seed lives in immutable login/respawn records on Fabric/NeoForge;
implementing the swap there needs a packet rebuild and is left out pending
testing. ² Update checking is Paper-only for now. ³ Fabric/NeoForge use the
operator permission level (op = bypass) instead of a chat command.

## Configuration

A single `config.yml` (see `common/src/main/resources/antiespguard/config.yml`)
is shared by every platform:

- Paper: `plugins/AntiESPGuard/config.yml`
- Fabric: `config/antiespguard/config.yml`
- NeoForge: `config/antiespguard/config.yml`

Key options: `hideBelowY`, `revealBelowYWhenUnder`, `maskBlock`,
`enabledEnvironments`, `disabledWorlds`, `maskEntities`,
`maskUndergroundPlayers`, `lazyUnmask.*`, `modDetection.*`, `antiSeedCracker.*`,
`updateChecker.*`.

## How the masking works

1. A player above `revealBelowYWhenUnder` is a **surface** player.
2. Every chunk packet sent to a surface player has all blocks below
   `hideBelowY` rewritten to `maskBlock` (STONE) — so a freecam camera flown
   underground only ever sees solid stone.
3. When the player descends below the line, the chunks around them are re-sent
   **unmasked** (progressive reveal), so normal play is unaffected.
4. Entities/players below `hideBelowY` are hidden from surface players
   (entity-radar / combat-ESP defence) without removing them from the tab list.

## Building

Requires JDK 21. Each platform module produces its own artifact:

```bash
./gradlew :paper:shadowJar       # -> paper/build/libs/AntiESPGuard-Paper-<ver>.jar
./gradlew :fabric:remapJar       # -> fabric/build/libs/...
./gradlew :neoforge:build        # -> neoforge/build/libs/...
```

The Paper jar requires the **PacketEvents** plugin on the server (declared as a
hard dependency in `plugin.yml`).

## License

MIT.
