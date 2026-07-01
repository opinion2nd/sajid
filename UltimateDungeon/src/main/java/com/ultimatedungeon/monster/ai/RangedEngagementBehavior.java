package com.ultimatedungeon.monster.ai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Keeps a ranged monster at its preferred distance: it closes in when the target
 * is too far and kites backwards when the target gets too close.
 */
public final class RangedEngagementBehavior {

    private final double preferredDistance;

    public RangedEngagementBehavior(final double preferredDistance) {
        this.preferredDistance = preferredDistance;
    }

    public void maintainDistance(@NotNull final LivingEntity monster, @NotNull final Player target) {
        final double dist = monster.getLocation().distance(target.getLocation());
        final Vector toTarget = target.getLocation().toVector()
                .subtract(monster.getLocation().toVector());
        if (toTarget.lengthSquared() < 1.0E-4) return;
        toTarget.normalize();
        if (dist < preferredDistance - 1.0) {
            monster.setVelocity(toTarget.multiply(-0.25).setY(0.05)); // kite back
        } else if (dist > preferredDistance + 2.0) {
            monster.setVelocity(toTarget.multiply(0.25).setY(0.05));  // close in
        }
    }
}
