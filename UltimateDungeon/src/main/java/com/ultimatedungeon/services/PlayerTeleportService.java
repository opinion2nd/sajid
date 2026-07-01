package com.ultimatedungeon.services;

import com.ultimatedungeon.core.PluginLogger;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Centralises all dungeon-related teleportation so entry/exit movement is
 * consistent and safe.
 *
 * <p>Uses Paper's async teleport where available (smoother chunk loading) and
 * falls back to a synchronous teleport on platforms that lack it.</p>
 */
public final class PlayerTeleportService {

    private final PluginLogger logger;

    public PlayerTeleportService(@NotNull final PluginLogger logger) {
        this.logger = logger;
    }

    /** Teleports a single player, loading the destination chunk first. */
    public void teleport(@NotNull final Player player, @NotNull final Location destination) {
        final Location target = destination.clone();
        if (target.getWorld() != null) {
            target.getWorld().getChunkAt(target).load(true);
        }
        try {
            player.teleportAsync(target);
        } catch (final NoSuchMethodError | NoClassDefFoundError legacy) {
            player.teleport(target);
        }
    }

    /** Teleports a whole group to the same destination. */
    public void teleportAll(@NotNull final Collection<? extends Player> players, @NotNull final Location destination) {
        for (final Player p : players) teleport(p, destination);
    }
}
