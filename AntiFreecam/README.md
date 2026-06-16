# AntiFreecam

An original, from-scratch anti-freecam / anti-ESP block masker for **Paper / Purpur 1.21.x**.
It hides everything below a configurable Y level from players who are on the surface, so a
freecam or ESP camera that flies underground sees nothing but stone — while normal underground
play stays fully vanilla.

> 100% original code (not derived from any closed-source plugin). MIT-style — it's yours to edit and ship.

## How it works

| Stage | Where | What |
|-------|-------|------|
| State tracking | `PlayerTracker` (main thread) | Marks each player surface vs underground, computes the reveal neighbourhood. |
| Masking | `ChunkMaskListener` (PacketEvents) | Rewrites every block below `hideBelowY` to `maskBlock` in outgoing chunk packets for masked players/chunks. |
| Reveal / re-mask | `ChunkResender` (NMS) | Re-sends chunks when a player crosses the surface/underground boundary so the change is seamless. |
| Entity hiding | `EntityMaskListener` | Drops spawn packets for entities below `hideBelowY` from surface players. |
| Mod detection | `BrandDetectionListener` | Best-effort `minecraft:brand` watch (Wurst by default). |

## Requirements

- Paper / Purpur **1.21.x**, Java **21**
- The **PacketEvents** plugin installed on the server (hard dependency)

## Build

```bash
gradle build      # or ./gradlew build once you generate a wrapper
```

The shaded jar lands in `build/libs/AntiFreecam-1.0.0.jar`. Drop it (and PacketEvents) into `plugins/`.

## Commands & permissions

- `/antifreecam reload` — reload `config.yml` (`antifreecam.reload`)
- `/antifreecam bypass [player]` — toggle masking bypass for staff (`antifreecam.bypass`)

## Notes / things to verify on a live server

- **Progressive reveal** depends on an NMS chunk re-send. If the server internals change, masking
  still works but reveal is skipped (a warning is logged once) — open an issue with your version.
- **Entity hiding** is experimental; a hidden entity reappears on its next spawn packet.
- **Mod detection** via brand is a *signal*, not proof — many clients spoof the brand.
