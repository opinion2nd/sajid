package com.ultimatedungeon.monster.ai;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Selects the most appropriate player target for a monster — the nearest living,
 * non-spectator player within the engagement radius.
 */
public final class TargetSelectorBehavior {

    @Nullable
    public Player selectTarget(@NotNull final LivingEntity monster, final double radius) {
        if (monster.getWorld() == null) return null;
        Player best = null;
        double bestDistSq = radius * radius;
        final Location origin = monster.getLocation();
        for (final Player player : monster.getWorld().getPlayers()) {
            if (player.isDead() || player.getGameMode() == org.bukkit.GameMode.SPECTATOR) continue;
            final double distSq = player.getLocation().distanceSquared(origin);
            if (distSq <= bestDistSq) {
                bestDistSq = distSq;
                best = player;
            }
        }
        return best;
    }
}
