package com.ultimatedungeon.monster.ai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Shares a freshly acquired target with nearby allied monsters so a pack reacts
 * together rather than aggroing one at a time.
 */
public final class GroupCoordinationBehavior {

    private final double shareRadius;

    public GroupCoordinationBehavior(final double shareRadius) {
        this.shareRadius = shareRadius;
    }

    public void shareTarget(@NotNull final LivingEntity source,
                            @NotNull final Player target,
                            @NotNull final Collection<LivingEntity> allies) {
        final double radiusSq = shareRadius * shareRadius;
        for (final LivingEntity ally : allies) {
            if (ally.equals(source) || ally.isDead()) continue;
            if (!ally.getWorld().equals(source.getWorld())) continue;
            if (ally.getLocation().distanceSquared(source.getLocation()) > radiusSq) continue;
            if (ally instanceof final Mob mob && mob.getTarget() == null) {
                mob.setTarget(target);
            }
        }
    }
}
