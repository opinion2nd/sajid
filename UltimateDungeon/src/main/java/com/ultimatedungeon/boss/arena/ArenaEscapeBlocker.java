package com.ultimatedungeon.boss.arena;

import com.ultimatedungeon.room.model.RoomData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Keeps players inside the boss arena: detects when a player crosses the room
 * boundary and pushes them back toward the centre.
 */
public final class ArenaEscapeBlocker {

    public boolean isOutside(@NotNull final RoomData arena, @NotNull final Location loc) {
        return !arena.contains(loc);
    }

    /** Nudges a player who has left the arena back toward its centre. */
    public void pushBack(@NotNull final Player player, @NotNull final RoomData arena) {
        final Location centre = arena.getCentre();
        if (centre.getWorld() == null || !centre.getWorld().equals(player.getWorld())) {
            return;
        }
        final Vector inward = centre.toVector().subtract(player.getLocation().toVector());
        if (inward.lengthSquared() < 1.0E-4) return;
        player.setVelocity(inward.normalize().multiply(0.8).setY(0.2));
    }
}
