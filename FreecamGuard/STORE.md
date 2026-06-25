# FreecamGuard

**Lightweight, packet-level Freecam & cheat-client detector for Paper/Purpur/Folia 1.21+.**
Catches the Freecam mod (and Meteor/Wurst) the moment a player joins and kicks them automatically — no client mod, no resource pack, no setup headaches.

---

## ✨ Features

- 🎯 **Freecam mod detection** — instantly identifies the popular client-side Freecam mod and kicks the player.
- 🧨 **Cheat-client detection** — also probes for **Meteor Client** and **Wurst**.
- 🪧 **Invisible sign-probe** — uses a server-only sign that never touches your world; players don't see fake blocks left behind.
- 🔒 **Strict / anti-dodge mode** — players who refuse to complete the check (clients that block the probe) are kicked too, so cheats with "anti-detection" can't just ignore it.
- 📡 **Brand & channel watch** — a second layer that reads the client brand and registered plugin channels for `freecam:` / `meteor-client:` / `wurst:`.
- 💬 **Fully customizable messages** — multi-line, colour-coded kick screens with `{mod}` / `{player}` placeholders.
- 🔔 **Staff alerts** — in-game notifications for online staff + console log on every detection.
- ⚙️ **Per-mod toggles, bypass permission, live reload.**
- ⚡ **Folia-ready**, async-safe, and extremely lightweight (no per-tick scanning).

---

## 🧠 How it works

Minecraft signs support *translation keys*. When the server shows a player a sign that contains a hidden key such as `key.freecam.toggle`:

- a **vanilla** client can't translate the unknown key, so it sends the **raw key** straight back;
- a client that ships the **Freecam mod** automatically replaces it with the mod's localized label ("Toggle Freecam").

FreecamGuard opens this server-only sign a couple of seconds after join, reads what comes back, and:

- **raw key returned** → clean client → nothing happens;
- **translated text returned** → mod detected → player is kicked with your message;
- **nothing / blanked returned (the client blocked the probe)** → treated as a dodge → kicked (strict mode).

The fake sign is never placed in your world — the original block is restored instantly, and the sign re-pops until the client answers so it can't be ignored.

---

## 📦 Requirements

- **Paper / Purpur / Folia / Leaf**, Minecraft **1.21+**
- **[PacketEvents](https://www.spigotmc.org/resources/packetevents-api.80279/)** installed on the server (hard dependency)
- Java **21**

---

## ⬇️ Installation

1. Put **PacketEvents** in your `plugins/` folder.
2. Put **FreecamGuard.jar** in `plugins/`.
3. Start the server once — `plugins/FreecamGuard/config.yml` is generated.
4. Edit the config if you like, then run `/freecamguard reload`.

---

## ⌨️ Commands

| Command | Description |
|---|---|
| `/freecamguard reload` | Reload `config.yml` |
| `/freecamguard status` | Show what is being detected & current settings |

Alias: **`/fcg`**

---

## 🔑 Permissions

| Permission | Default | Description |
|---|---|---|
| `freecamguard.admin` | op | Use `/freecamguard` commands |
| `freecamguard.notify` | op | Receive in-game detection alerts |
| `freecamguard.bypass` | false | Exempt a player from the probe (e.g. trusted staff) |

---

## ⚙️ Configuration

```yaml
modDetection:
  probeOnJoin: true          # run the check when a player joins
  probeDelayTicks: 40        # delay before the sign pops (20 ticks = 1s)
  probeTimeoutTicks: 300     # how long to wait for an answer (300 = 15s)
  autoKick: true             # kick on detection
  kickOnNoResponse: true     # STRICT: kick clients that ignore/block the sign
  notifyAdmins: true         # alert freecamguard.notify staff
  kickMessage: "&c&l✖ Disallowed Mod Detected\n \n&7You are using &c&l{mod}&7.\n&7This is &cnot allowed&7 on this server.\n&7Remove it and rejoin to play."
  noResponseKickMessage: "&c&l✖ Anti-Cheat Check Failed\n \n&7You did not close the verification sign.\n&7Please &f&lrejoin&7 and &fclose the sign&7 that pops up."
  detect:
    Freecam: true
    Meteor Client: true
    Wurst: true

brandDetection:
  notifyAdmins: true         # alert on modded brand / cheat channel
  kickOnCheatChannel: true   # kick on a known cheat plugin channel
```

**Message formatting:** use `&` colour codes and `\n` for new lines. `{mod}` = detected mod, `{player}` = player name.

---

## ❓ FAQ

**Does it need a client mod or resource pack?**
No. It is 100% server-side.

**Will it kick legit players?**
A normal player just closes the sign (it's a quick pop-up) and is verified instantly. Only clients that **block/ignore** the verification sign are kicked by strict mode. Set `kickOnNoResponse: false` if you prefer not to kick non-responders.

**Bedrock (Geyser/Floodgate) players?**
Automatically skipped — the check is Java-only.

**Can it catch every cheat client?**
The sign-probe reliably catches mods that expose translatable keys (like Freecam). Some clients ship "anti-detection" that blocks the probe — strict mode kicks those as non-responders. A few clients (e.g. very recent Wurst) patched the translation leak entirely; for movement/combat cheats (fly, killaura, reach) pair this with a behavioural anti-cheat. For Freecam/ESP *vision*, pair it with a block-masking plugin.

---

## 🛠 Support & Updates

Issues and feature requests welcome. Tested on Paper 1.21.x with PacketEvents 2.5.x.
