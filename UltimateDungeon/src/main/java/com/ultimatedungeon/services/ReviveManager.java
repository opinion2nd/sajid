package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import com.ultimatedungeon.dungeon.instance.DungeonInstance;
import com.ultimatedungeon.dungeon.instance.DungeonInstanceManager;
import com.ultimatedungeon.util.MiniMessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Party revive system.
 *
 * <p>When a party member dies with teammates still alive, they are NOT kicked
 * out: they become a spectator hovering at their death spot, marked by a soul
 * beacon of particles. A living teammate who <b>sneaks within 2.5 blocks for
 * {@code hold-seconds}</b> revives them on the spot with half health. If nobody
 * revives them within {@code timeout-seconds}, they are sent home — and if
 * that leaves the dungeon empty, the run fails.</p>
 *
 * <p>Tunables live in party.yml under {@code revive}.</p>
 */
public final class ReviveManager {

    /** One downed (revivable) player. */
    private static final class Downed {
        final UUID instanceId;
        final Location deathSpot;
        final long downedAt = System.currentTimeMillis();
        int reviveProgressTicks; // task runs every 10 ticks

        Downed(final UUID instanceId, final Location deathSpot) {
            this.instanceId = instanceId;
            this.deathSpot = deathSpot;
        }
    }

    private final DungeonInstanceManager instanceManager;
    private final PluginLogger logger;
    private final boolean enabled;
    private final int holdSeconds;
    private final int timeoutSeconds;

    private final Map<UUID, Downed> downed = new ConcurrentHashMap<>();

    /** Fired when a downed player times out and must leave (player). */
    private java.util.function.Consumer<Player> onTimeout = p -> {};

    public ReviveManager(@NotNull final DungeonInstanceManager instanceManager,
                         @NotNull final com.ultimatedungeon.config.files.PartyConfig partyConfig,
                         @NotNull final PluginLogger logger) {
        this.instanceManager = instanceManager;
        this.logger = logger;
        this.enabled = partyConfig.isReviveEnabled();
        this.holdSeconds = partyConfig.getReviveHoldSeconds();
        this.timeoutSeconds = partyConfig.getReviveTimeoutSeconds();
    }

    public boolean isEnabled() { return enabled; }

    public void setTimeoutHook(@NotNull final java.util.function.Consumer<Player> hook) {
        this.onTimeout = hook;
    }

    public boolean isDowned(@NotNull final Player player) {
        return downed.containsKey(player.getUniqueId());
    }

    /** Puts a dead party member into the downed (spectator) state at their body. */
    public void down(@NotNull final DungeonInstance instance, @NotNull final Player player,
                     @NotNull final Location deathSpot) {
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(deathSpot.clone().add(0, 1, 0));
        downed.put(player.getUniqueId(), new Downed(instance.getInstanceId(), deathSpot.clone()));
        MiniMessageUtil.send(player, "<red>You are down!</red> <gray>A teammate can revive you "
                + "by sneaking at your body for <yellow>" + holdSeconds + "s</yellow>. "
                + "Auto-leave in <yellow>" + timeoutSeconds + "s</yellow>.");
        logger.debug("Player downed: " + player.getName());
    }

    /** One revive tick — runs every 10 ticks from the scheduler. */
    public void tick() {
        if (!enabled || downed.isEmpty()) return;
        for (final Map.Entry<UUID, Downed> entry : Map.copyOf(downed).entrySet()) {
            final Player dead = Bukkit.getPlayer(entry.getKey());
            final Downed state = entry.getValue();

            if (dead == null || !dead.isOnline()) {
                downed.remove(entry.getKey());
                continue;
            }
            // Timed out — send them home.
            if (System.currentTimeMillis() - state.downedAt > timeoutSeconds * 1000L) {
                downed.remove(entry.getKey());
                dead.setGameMode(GameMode.SURVIVAL);
                MiniMessageUtil.send(dead, "<red>Nobody reached you in time...");
                onTimeout.accept(dead);
                continue;
            }

            // Soul beacon so teammates can find the body.
            final var world = state.deathSpot.getWorld();
            if (world != null) {
                world.spawnParticle(Particle.SOUL, state.deathSpot.clone().add(0, 1, 0),
                        6, 0.2, 0.6, 0.2, 0.01);
                world.spawnParticle(Particle.END_ROD, state.deathSpot.clone().add(0, 2.2, 0),
                        2, 0.1, 0.4, 0.1, 0.0);
            }

            // A living, sneaking teammate within reach revives.
            final Player reviver = findReviver(state, dead);
            if (reviver != null) {
                state.reviveProgressTicks++;
                final int needed = holdSeconds * 2; // task period = 10 ticks
                final int secondsLeft = Math.max(0, (needed - state.reviveProgressTicks + 1) / 2);
                MiniMessageUtil.sendActionBar(reviver, "<green>Reviving " + dead.getName()
                        + "... <yellow>" + secondsLeft + "s");
                MiniMessageUtil.sendActionBar(dead, "<green>" + reviver.getName()
                        + " is reviving you... <yellow>" + secondsLeft + "s");
                if (state.reviveProgressTicks >= needed) {
                    downed.remove(entry.getKey());
                    revive(dead, reviver, state);
                }
            } else if (state.reviveProgressTicks > 0) {
                state.reviveProgressTicks = 0; // reviver stepped away — reset
            }
        }
    }

    /** Restores every downed player of an instance (cleanup/fail/complete path). */
    public void clearInstance(@NotNull final UUID instanceId) {
        for (final Map.Entry<UUID, Downed> entry : Map.copyOf(downed).entrySet()) {
            if (!entry.getValue().instanceId.equals(instanceId)) continue;
            downed.remove(entry.getKey());
            final Player p = Bukkit.getPlayer(entry.getKey());
            if (p != null && p.isOnline() && p.getGameMode() == GameMode.SPECTATOR) {
                p.setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    /** Restores everyone (plugin shutdown). */
    public void restoreAll() {
        for (final UUID id : Map.copyOf(downed).keySet()) {
            final Player p = Bukkit.getPlayer(id);
            if (p != null && p.getGameMode() == GameMode.SPECTATOR) {
                p.setGameMode(GameMode.SURVIVAL);
            }
        }
        downed.clear();
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private Player findReviver(@NotNull final Downed state, @NotNull final Player dead) {
        final var world = state.deathSpot.getWorld();
        if (world == null) return null;
        for (final Player p : world.getPlayers()) {
            if (p.equals(dead) || downed.containsKey(p.getUniqueId())) continue;
            if (p.getGameMode() == GameMode.SPECTATOR || p.isDead() || !p.isSneaking()) continue;
            if (instanceManager.getInstanceForPlayer(p) == null) continue;
            if (p.getLocation().distanceSquared(state.deathSpot) <= 2.5 * 2.5) return p;
        }
        return null;
    }

    private void revive(@NotNull final Player dead, @NotNull final Player reviver,
                        @NotNull final Downed state) {
        dead.setGameMode(GameMode.SURVIVAL);
        dead.teleport(state.deathSpot.clone().add(0, 0.5, 0));
        final var maxAttr = dead.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        final double max = maxAttr != null ? maxAttr.getValue() : 20.0;
        dead.setHealth(Math.max(1.0, max / 2.0));
        dead.setFoodLevel(Math.max(dead.getFoodLevel(), 10));
        dead.addPotionEffect(new org.bukkit.potion.PotionEffect(
                org.bukkit.potion.PotionEffectType.RESISTANCE, 100, 1));

        final var world = state.deathSpot.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.TOTEM_OF_UNDYING,
                    dead.getLocation().add(0, 1, 0), 60, 0.6, 1.0, 0.6, 0.2);
            world.playSound(dead.getLocation(), Sound.ITEM_TOTEM_USE, 0.8f, 1.2f);
        }
        MiniMessageUtil.send(dead, "<green><bold>REVIVED!</bold></green> <gray>by "
                + reviver.getName() + " — back to the fight!");
        MiniMessageUtil.send(reviver, "<green>You revived " + dead.getName() + "!");
        logger.debug("Player revived: " + dead.getName() + " by " + reviver.getName());
    }
}
