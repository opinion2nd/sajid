# FreecamGuard

A small, **original** PacketEvents plugin that detects the **Freecam** mod and
the **Meteor** / **Wurst** cheat clients and **kicks** them. Written from
scratch — it shares no code with any other plugin.

**No X-ray feature. No minimap detection.** Just freecam + cheat-client
detection and kicking, exactly as requested.

## How it works

1. **Sign probe** (`SignProbeListener`) — a few seconds after a player joins,
   the server shows them an invisible, server-only sign editor whose lines are
   *translation keys* (`key.freecam.toggle`, `key.meteor-client.open-gui`,
   `key.wurst.zoom`). A vanilla client sends the raw key back; a client that has
   the matching mod replaces it with the mod's translated label. That difference
   reveals the mod, and the player is kicked. The fake sign is never placed in
   the world — the real block is restored immediately.
2. **Brand / channel watch** (`BrandChannelListener`) — reads the client brand
   and registered plugin channels (`freecam:`, `meteor-client:`, `wurst:`) as a
   backup signal, and kicks on a known cheat channel.

Bedrock players (Geyser/Floodgate) are skipped automatically.

## Requirements

- Paper / Purpur / Folia / Leaf, **MC 1.21+**
- The **PacketEvents** plugin installed on the server (hard dependency).

## Build

This needs the PaperMC and CodeMC maven repositories, which are **blocked in the
Claude Code web sandbox** (org egress policy), so the jar was **not** built here.
On any machine with normal internet it builds with one command:

```bash
cd FreecamGuard
./gradlew shadowJar
```

The finished plugin lands at `build/libs/FreecamGuard.jar`. Drop it (plus
PacketEvents) into your server's `plugins/` folder.

## Commands & permissions

- `/freecamguard reload` — reload `config.yml`
- `/freecamguard status` — show what is being detected
- `freecamguard.admin` (op) — use the command
- `freecamguard.notify` (op) — receive in-game detection alerts
- `freecamguard.bypass` (false) — exempt a player from the probe

## Config (`config.yml`)

```yaml
modDetection:
  probeOnJoin: true
  probeDelayTicks: 100        # 20 ticks = 1 second
  autoKick: true
  kickMessage: "&cYou are using a disallowed mod ({mod}) ..."
  notifyAdmins: true
  detect:
    Freecam: true
    Meteor Client: true
    Wurst: true
brandDetection:
  notifyAdmins: true
  kickOnCheatChannel: true
```

## Layout

```
FreecamGuard/
  build.gradle.kts, settings.gradle.kts, gradlew, gradle/   # build files
  src/main/resources/  plugin.yml, config.yml
  src/main/java/dev/opinion2nd/freecamguard/
    FreecamGuardPlugin.java        # bootstrap + PacketEvents init
    SchedulerUtil.java             # Bukkit + Folia scheduling
    detect/SignProbeListener.java  # the sign-probe detector + kick
    detect/BrandChannelListener.java
    command/FreecamGuardCommand.java
```

## Ownership

Every line here is original. The sign-probe and brand-channel ideas are common,
publicly known anti-cheat techniques; the implementation is your own and carries
no third-party copyright.
