package com.ultimatedungeon.dungeon.lifecycle;

import com.ultimatedungeon.services.NotificationService;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Down-and-revive system for multiplayer dungeons.
 *
 * <p>When a player would take fatal damage but still has living teammates, they
 * are "downed" into spectator mode at their death spot instead of dying. A
 * teammate who stands close for {@link #REVIVE_SECONDS} brings them back at half
 * health. If nobody reaches them within {@link #BLEED_OUT_SECONDS} they stay
 * down (spectating) until the run ends. The actual death of the last standing
 * player is what fails the dungeon — handled by the death listener — so this
 * class never has to detect a wipe itself.</p>
 */
public final class ReviveManager {

    private static final int REVIVE_SECONDS = 5;
    private static final int BLEED_OUT_SECONDS = 30;
    private static final double REVIVE_RADIUS = 3.0;
    private static final double REVIVE_RADIUS_SQ = REVIVE_RADIUS * REVIVE_RADIUS;

    private static final class Downed {
        final UUID instanceId;
        final Location spot;
        final GameMode originalMode;
        int reviveProgress;
        int bleedOutLeft = BLEED_OUT_SECONDS;

        Downed(final UUID instanceId, final Location spot, final GameMode originalMode) {
            this.instanceId = instanceId;
            this.spot = spot;
            this.originalMode = originalMode;
        }
    }

    private final DungeonLauncher launcher;
    private final NotificationService notifications;
    private final Map<UUID, Downed> downed = new ConcurrentHashMap<>();

    public ReviveManager(@NotNull final DungeonLauncher launcher,
                         @NotNull final NotificationService notifications) {
        this.launcher = launcher;
        this.notifications = notifications;
    }

    /** @return true if {@code player} is currently downed. */
    public boolean isDown(@NotNull final Player player) {
        return downed.containsKey(player.getUniqueId());
    }

    /**
     * @return true if the instance has at least one standing (not downed,
     *         not spectating) player other than {@code player} — i.e. someone
     *         who could revive a downed teammate.
     */
    public boolean hasAliveTeammate(@NotNull final Player player, @NotNull final UUID instanceId) {
        for (final Player other : launcher.getPlayers(instanceId)) {
            if (other.equals(player)) continue;
            if (!other.isOnline() || other.isDead()) continue;
            if (isDown(other) || other.getGameMode() == GameMode.SPECTATOR) continue;
            return true;
        }
        return false;
    }

    /** Downs a player instead of letting them die. */
    public void down(@NotNull final Player player, @NotNull final UUID instanceId) {
        final Location spot = player.getLocation().clone();
        downed.put(player.getUniqueId(), new Downed(instanceId, spot, player.getGameMode()));
        player.setHealth(Math.min(player.getHealth() <= 0 ? 1.0 : player.getHealth(), player.getMaxHealth()));
        player.setGameMode(GameMode.SPECTATOR);
        player.teleport(spot);
        notifications.actionBar(player, "<red>You are down! <gray>A teammate can revive you.");
        for (final Player other : launcher.getPlayers(instanceId)) {
            if (!other.equals(player)) {
                notifications.chat(other, "<red>" + player.getName()
                        + " is down! <gray>Stand near them to revive.");
            }
        }
    }

    /** Per-second tick: advances revives and bleed-out timers. */
    public void tick() {
        for (final Map.Entry<UUID, Downed> entry : downed.entrySet()) {
            final Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            final Downed state = entry.getValue();
            if (player == null || !player.isOnline()) {
                downed.remove(entry.getKey());
                continue;
            }
            final Player reviver = nearbyReviver(player, state);
            if (reviver != null) {
                state.reviveProgress++;
                spawnReviveBeacon(state.spot);
                notifications.actionBar(player, "<yellow>Being revived by <white>"
                        + reviver.getName() + " <gray>(" + state.reviveProgress + "/" + REVIVE_SECONDS + ")");
                notifications.actionBar(reviver, "<green>Reviving <white>" + player.getName()
                        + " <gray>(" + state.reviveProgress + "/" + REVIVE_SECONDS + ")");
                if (state.reviveProgress >= REVIVE_SECONDS) {
                    revive(player, state);
                }
            } else {
                state.reviveProgress = 0;
                state.bleedOutLeft--;
                if (state.bleedOutLeft > 0 && state.bleedOutLeft <= 10) {
                    notifications.actionBar(player, "<dark_red>Bleeding out… <red>" + state.bleedOutLeft + "s");
                }
                // On bleed-out the player simply stays down (spectating) until the
                // dungeon ends; the run only fails when the last standing player dies.
            }
        }
    }

    private Player nearbyReviver(@NotNull final Player downedPlayer, @NotNull final Downed state) {
        for (final Player other : launcher.getPlayers(state.instanceId)) {
            if (other.equals(downedPlayer) || isDown(other)) continue;
            if (!other.isOnline() || other.isDead() || other.getGameMode() == GameMode.SPECTATOR) continue;
            if (!other.getWorld().equals(state.spot.getWorld())) continue;
            if (other.getLocation().distanceSquared(state.spot) <= REVIVE_RADIUS_SQ) return other;
        }
        return null;
    }

    private void revive(@NotNull final Player player, @NotNull final Downed state) {
        downed.remove(player.getUniqueId());
        player.setGameMode(state.originalMode == GameMode.SPECTATOR ? GameMode.SURVIVAL : state.originalMode);
        player.teleport(state.spot);
        player.setHealth(Math.max(1.0, player.getMaxHealth() / 2.0));
        player.setFireTicks(0);
        notifications.title(player, "<green>Revived!", "<gray>Back in the fight");
        player.playSound(player.getLocation(), Sound.ITEM_TOTEM_USE, 1.0f, 1.2f);
        if (player.getWorld() != null) {
            player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 12, 0.5, 0.5, 0.5, 0.1);
        }
    }

    private void spawnReviveBeacon(@NotNull final Location spot) {
        if (spot.getWorld() != null) {
            spot.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, spot.clone().add(0, 1, 0), 6, 0.4, 0.6, 0.4, 0.0);
        }
    }

    /**
     * Restores any players still downed in this instance to normal play (called
     * on teardown) so nobody is left stuck in spectator after being sent home.
     */
    public void clearInstance(@NotNull final UUID instanceId) {
        for (final Map.Entry<UUID, Downed> entry : Map.copyOf(downed).entrySet()) {
            if (!entry.getValue().instanceId.equals(instanceId)) continue;
            downed.remove(entry.getKey());
            final Player player = org.bukkit.Bukkit.getPlayer(entry.getKey());
            if (player != null && player.getGameMode() == GameMode.SPECTATOR) {
                final GameMode mode = entry.getValue().originalMode;
                player.setGameMode(mode == GameMode.SPECTATOR ? GameMode.SURVIVAL : mode);
            }
        }
    }

    /** Exposes the online players of an instance (used by combat listener wiring). */
    @NotNull
    public List<Player> instancePlayers(@NotNull final UUID instanceId) {
        return launcher.getPlayers(instanceId);
    }
}
