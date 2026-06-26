# CustomBoss

A standalone **Spigot / Paper** plugin that spawns a fully configurable custom
boss with a boss bar, special attacks, and custom rewards.

- **Tested API:** Paper / Spigot **1.21.x** (`api-version: 1.21`)
- **Java:** 21

## Features

- **Boss bar + custom health** — a named boss bar that scales to the boss's
  configured health and only shows to nearby players.
- **Special attacks** (one fires every `interval-seconds`):
  - 🔥 **Fireball** — launches an explosive, incendiary fireball at a player.
  - 🧟 **Summon minions** — spawns a configurable wave of helpers.
  - 💥 **AOE shockwave** — knocks back and damages everyone in range.
  - ✨ **Teleport** — blinks to a random nearby player.
  - 😡 **Enrage** — permanently speeds up once it drops below a health threshold.
- **Custom drops / rewards** — clears vanilla loot and drops your configured
  items, runs console reward commands, and broadcasts a kill message.
- **Spawn command + config** — spawn from a command and tune everything in
  `config.yml`.

## Commands

| Command         | Description                          | Permission          |
|-----------------|--------------------------------------|---------------------|
| `/boss spawn`   | Spawn a boss at your location        | `customboss.command`|
| `/boss killall` | Remove all active bosses             | `customboss.command`|
| `/boss list`    | Show how many bosses are active      | `customboss.command`|
| `/boss reload`  | Reload `config.yml`                  | `customboss.command`|

`customboss.command` defaults to **op**.

## Configuration

Everything lives in `config.yml`. Highlights:

```yaml
boss:
  display-name: "&c&lInferno Warlord"
  entity-type: WITHER_SKELETON   # any living entity type
  health: 500.0
  damage: 12.0
  abilities:
    interval-seconds: 6
    summon-minions: { enabled: true, type: ZOMBIE, count: 3 }
    aoe-knockback:  { enabled: true, radius: 6.0, damage: 8.0, strength: 2.0 }
  rewards:
    broadcast: "&6The boss has been slain by &e{player}&6!"
    commands: [ "give {player} diamond 5" ]
    drops: [ "DIAMOND 5", "NETHERITE_INGOT 1" ]
```

`{player}` in rewards is replaced by the killer's name. Run `/boss reload`
after editing — new settings apply to bosses spawned afterwards.

## Building

```bash
./gradlew build
```

The plugin jar is written to `build/libs/CustomBoss-<version>.jar`. Drop it in
your server's `plugins/` folder and restart.
