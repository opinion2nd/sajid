# AntiFreecam

An original, from-scratch anti-freecam / anti-x-ray block masker for
**Paper / Purpur**. Below a configurable Y level it occlusion-masks every
fully-buried block (replacing it with an opaque `maskBlock`, default **deepslate**),
so a freecam or x-ray camera that flies into solid rock finds nothing but solid
rock — while the caves and mines you can actually reach stay completely vanilla.

> A server cannot detect whether a client has freecam toggled (it is 100%
> client-side), and it sends the **same** blocks to your real camera and a freecam
> camera. So the only way *your* view stays vanilla is to keep the hidden volume an
> opaque solid; freecam then just hits rock instead of seeing ores/caves.

## How it works

Below `hideBelowY`, a solid block is rewritten to `maskBlock` (an opaque solid)
only if it is **fully buried** — every one of its six neighbours is also a solid
block. Any block that touches a cave, tunnel or dug-out space is left untouched.

- The parts of the world you can legitimately see render exactly like vanilla —
  **no fake stone, no radius cliffs** (you never see the fill; it is always behind
  a wall you can't see through).
- Ore veins buried inside solid rock are hidden, so freecam / x-ray sees only rock.
- Mining stays vanilla: breaking a block instantly reveals the real block behind it.

| Stage | Where | What |
|-------|-------|------|
| State tracking | `PlayerTracker` (main thread) | Tracks each player's bypass + active-world flags. |
| Masking | `ChunkMaskListener` (PacketEvents) | Occlusion-masks fully-buried blocks below `hideBelowY` to `maskBlock` (DEEPSLATE) in outgoing chunk packets. |
| Reveal-on-mine | `BlockRevealListener` | Reveals newly-exposed blocks on break, re-hides on place, re-sends chunks after explosions. |
| Entity hiding | `EntityMaskListener` | Optional anti entity-ESP, **off by default**. |
| Mod detection | `BrandDetectionListener` | Best-effort `minecraft:brand` watch (Wurst by default). |

## Requirements

- Bukkit / Spigot / Paper / Purpur, **Minecraft 1.18 → 1.21.11 / 26.x**, Java **21**
- The **PacketEvents** plugin installed on the server (hard dependency) — install a
  PacketEvents build that supports your Minecraft version (2.13.0+ for 1.21.11 / 26.x)

> Notes: **Folia is not yet supported** (its region scheduler needs separate work).
> On Spigot/Bukkit the explosion reveal-on-mine step is skipped (it uses Paper NMS);
> the core anti-freecam masking and break/place reveal still work everywhere.
> Versions below 1.21 should load but are not live-tested — verify before relying on them.

## Build

```bash
./gradlew build
```

The shaded jar lands in `build/libs/AntiFreecam-1.0.0.jar`. Drop it (and
PacketEvents) into `plugins/`.

See **[HOW_IT_WORKS.md](HOW_IT_WORKS.md)** for a short Bengali explainer.

## Commands & permissions

- `/antifreecam reload` — reload `config.yml` (`antifreecam.reload`)
- `/antifreecam bypass [player]` — toggle masking bypass for staff (`antifreecam.bypass`)

## Notes / things to verify on a live server

- **`maskBlock` must be an opaque solid** (default `DEEPSLATE`). Do not set it to
  `AIR`: that makes the buried rock see-through for *everyone*, including normal
  players, so your own view stops looking vanilla.
- A thin one-block "shell" on chunk borders is visible to freecam (it never
  reveals buried veins); it can be tightened later by reading neighbour chunks.
- Piston / fluid-driven exposures are not reveal-handled (rare below Y20; they
  self-heal on the next chunk reload). Break / place / explosions are handled.
