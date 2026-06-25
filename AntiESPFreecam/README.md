# AntiESPFreecam 1.3.3 — Freecam + Cheat-Client detection (no X-ray, no Minimap)

This is a **full copy** of the `AntiESPFreecam` Paper/Purpur/Folia/Leaf plugin
(v1.3.3, by Tuxdev, depends on **PacketEvents**), with two features turned off
as requested:

| Feature | Status in this build |
|---|---|
| Freecam / ESP block masking (hides blocks & players below Y from surface players) | **ON** |
| Mod detection — **Freecam** | **ON** (detected → kicked) |
| Mod detection — **Meteor Client** (hack client) | **ON** (detected → kicked) |
| Mod detection — **Wurst** (hack client) | **ON** (detected → kicked) |
| Mod detection — **Xaero's Minimap** | **OFF** (minimap is allowed) |
| **Anti-Xray** (the X-ray ore-hiding feature) | **OFF** (removed) |
| Brand detection (admin notify) | ON |
| Anti seed-cracker | ON |
| Update checker | ON |

So: if someone joins with a **Freecam mod** or a **hack client (Meteor/Wurst)**,
they are **kicked** automatically. The **minimap** is no longer flagged, and the
**X-ray** feature is disabled.

## How it detects mods and kicks

On join the plugin runs a **sign probe** (`f` class): it sends the client a
fake sign editor with hidden translation keys (`key.freecam.toggle`,
`key.meteor-client.open-gui`, `key.wurst.zoom`). If the client *translates* one
of those keys, the matching mod is present. When `modDetection.autoKick: true`
(default), the player is kicked with `modDetection.kickMessage`.

The `gui.xaero_enlarge_map` (Xaero's Minimap) probe entry is disabled via
`modDetection.detect."Xaero's Minimap": false`, so the minimap is never flagged.

## What exactly was changed

**Only `config.yml` was modified** — every compiled class is byte-for-byte
identical to the original jar (verified). The changes:

1. `antiXray.enabled: true` → `false`  → the X-ray feature is fully off.
   (When disabled, the plugin builds empty ore tables and never rewrites ore
   blocks in chunk packets.)
2. `modDetection.detect."Xaero's Minimap": true` → `false`  → minimap is no
   longer probed or kicked.

## Install

1. Install **PacketEvents** (Spigot/Paper build) on your server — it's a hard
   dependency (`depend: [packetevents]`).
2. Drop `AntiESPFreecam.jar` into `plugins/`.
3. Start the server once. The bundled `config.yml` (this build's, with X-ray and
   minimap off) is written to `plugins/AntiESPFreecam/config.yml`.
4. `/antiesp reload` after editing config. Commands: `/antiesp <reload|bypass [player]>`.

> If you are upgrading an existing install, your old `config.yml` is kept. To get
> these defaults, set `antiXray.enabled: false` and
> `modDetection.detect."Xaero's Minimap": false` yourself, or replace the file
> with the `config.yml` in this folder, then `/antiesp reload`.

## Folders

- `AntiESPFreecam.jar` — the ready-to-use plugin.
- `config.yml` — the bundled config of this build (X-ray off, minimap off).
- `source-decompiled/` — decompiled source (CFR), for reference only. The
  original jar is obfuscated (classes `a`–`g`), so names are auto-generated and
  the source is **not** meant to be recompiled as-is.
