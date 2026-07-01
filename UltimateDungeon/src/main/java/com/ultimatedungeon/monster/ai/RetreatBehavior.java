package com.ultimatedungeon.monster.ai;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Makes a badly wounded monster back away from its target instead of pressing
 * the attack, giving combat a more dynamic feel.
 */
public final class RetreatBehavior {

    private static final double LOW_HEALTH_FRACTION = 0.25;

    public boolean shouldRetreat(@NotNull final LivingEntity monster) {
        final double max = monster.getMaxHealth();
        return max > 0 && (monster.getHealth() / max) <= LOW_HEALTH_FRACTION;
    }

    public void retreat(@NotNull final LivingEntity monster, @NotNull final Player target) {
        final Vector away = monster.getLocation().toVector()
                .subtract(target.getLocation().toVector());
        if (away.lengthSquared() < 1.0E-4) return;
        monster.setVelocity(away.normalize().multiply(0.35).setY(0.1));
    }
}
