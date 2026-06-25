# AntiFreecam

An original, from-scratch anti-freecam / anti-x-ray block masker for
**Paper / Purpur 1.21.x**. Below a configurable Y level it sends fully-buried
blocks to the client as **void (air)**, so a freecam or x-ray camera that flies
into solid rock sees nothing — while the caves and mines you can actually reach
stay completely vanilla.

> A server cannot detect whether a client has freecam toggled (it is 100%
> client-side), so this plugin does not try to. It simply never sends the data
> freecam would need.

## How it works

Below `hideBelowY`, a solid block is rewritten to **air** only if it is **fully
buried** — every one of its six neighbours is also a solid block. Any block that
touches a cave, tunnel or dug-out space is left untouched.

- The parts of the world you can legitimately see render exactly like vanilla —
  **no fake stone, no radius cliffs**.
- Ore veins buried inside solid rock are never sent, so freecam / x-ray sees void.
- Mining stays vanilla: breaking a block instantly reveals the block behind it, so
  there are **no "void holes"**.

| Stage | Where | What |
|-------|-------|------|
| State tracking | `PlayerTracker` (main thread) | Tracks each player's bypass + active-world flags. |
| Masking | `ChunkMaskListener` (PacketEvents) | Occlusion-masks fully-buried blocks below `hideBelowY` to `maskBlock` (AIR) in outgoing chunk packets. |
| Reveal-on-mine | `BlockRevealListener` | Reveals newly-exposed blocks on break, re-hides on place, re-sends chunks after explosions. |
| Entity hiding | `EntityMaskListener` | Optional anti entity-ESP, **off by default**. |
| Mod detection | `BrandDetectionListener` | Best-effort `minecraft:brand` watch (Wurst by default). |

## Requirements

- Paper / Purpur **1.21.x**, Java **21**
- The **PacketEvents** plugin installed on the server (hard dependency)

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

- **Pure-air fill** is the most freecam-proof option; if you ever see lighting
  artifacts near caves on your build, set `maskBlock` to `STONE`/`DEEPSLATE`.
- A thin one-block "shell" on chunk borders is visible to freecam (it never
  reveals buried veins); it can be tightened later by reading neighbour chunks.
- Piston / fluid-driven exposures are not reveal-handled (rare below Y20; they
  self-heal on the next chunk reload). Break / place / explosions are handled.
