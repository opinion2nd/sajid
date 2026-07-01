package com.ultimatedungeon.monster.ai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Pushes a melee monster slightly sideways as it approaches so groups surround a
 * target from multiple angles instead of stacking in a single line.
 */
public final class FlankingBehavior {

    public void flank(@NotNull final LivingEntity monster, @NotNull final Player target, final boolean clockwise) {
        final Vector toTarget = target.getLocation().toVector()
                .subtract(monster.getLocation().toVector());
        if (toTarget.lengthSquared() < 1.0E-4) return;
        toTarget.normalize();
        // Perpendicular vector in the XZ plane.
        final Vector side = new Vector(-toTarget.getZ(), 0, toTarget.getX())
                .multiply(clockwise ? 0.18 : -0.18);
        monster.setVelocity(monster.getVelocity().add(side));
    }
}
