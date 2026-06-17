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

## Why "digging into a wall" after freecam is expected, not a bug

A lot of people see this and assume the server is lagging or broken. It isn't — here's exactly
what's happening and why.

**What the plugin actually reveals.** The server only sends *real* blocks for the small area
around a player's real, physical position (their actual character, moved by walking/flying
normally). Everywhere else below `hideBelowY` that the player hasn't actually walked/dug into is
sent to their client as solid `maskBlock` (stone by default) — it doesn't matter what's really
there (cave, ore, air, anything).

**What freecam does.** A freecam mod only detaches the *camera* — it flies the view around
without moving the real player entity. The server has no idea the camera went anywhere, so it
never reveals real terrain for the area the camera is looking at. The freecam user just sees the
masked fake-stone version of everything outside their real position's bubble, dressed up to look
like normal terrain.

**Why digging fails there.** If that player then tries to mine a block they only "saw" through
the floating camera (not blocks near where their real body actually is), the client thinks it's
breaking solid stone — but the server's real block at that position is something else (air, a
different block, whatever is actually there). The break request doesn't match what the server
actually has, so the server refuses it: nothing breaks, and it feels like hitting an invisible
wall. This is the plugin doing its job, not lag and not a server problem.

**Why a refresh "fixes" it.** Two things force the server to send the *real* blocks for an area:

1. The player's real body actually walks/digs close enough to that spot — `PlayerTracker`
   notices their real position changed, recomputes the reveal radius, and resends those chunks
   with true data (see `PlayerTracker.refresh`).
2. The player rejoins the server (or otherwise gets a fresh full chunk load) — this forces a
   clean resync of every nearby chunk based on their current real position, wiping out any stale
   masked chunks the client had cached from earlier.

So in short: you can always dig normally through ground you've genuinely walked/dug to on foot —
that part was always sent truthfully. You can never pre-dig or tunnel toward something you only
spotted with a detached freecam camera; that boundary only opens once your real character
actually gets there, or you reconnect.

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
