package com.ultimatedungeon.trap.engine;

import com.ultimatedungeon.trap.model.TrapTriggerType;
import com.ultimatedungeon.trap.traps.AbstractTrap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/** Detects when a player steps within a movement-triggered trap's radius. */
public final class TrapTriggerDetector {

    /** Activates any armed movement traps the player is standing within. */
    public void checkMovement(@NotNull final Player player, @NotNull final Collection<AbstractTrap> traps) {
        final Location playerLoc = player.getLocation();
        for (final AbstractTrap trap : traps) {
            if (trap.getDefinition().getTriggerType() != TrapTriggerType.PLAYER_MOVEMENT) continue;
            if (!trap.canTrigger()) continue;
            final Location loc = trap.getLocation();
            if (loc == null || loc.getWorld() == null || !loc.getWorld().equals(playerLoc.getWorld())) continue;
            final double radius = Math.max(1.0, trap.getDefinition().getTriggerRadius());
            if (loc.distanceSquared(playerLoc) <= radius * radius) {
                trap.activate();
            }
        }
    }
}
