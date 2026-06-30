package com.ultimatedungeon.boss.engine;

import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/** Tracks a boss entity's health as a 0–1 ratio of its configured maximum. */
public final class BossHealthTracker {

    private final LivingEntity boss;
    private final double maxHealth;

    public BossHealthTracker(@NotNull final LivingEntity boss, final double maxHealth) {
        this.boss = boss;
        this.maxHealth = Math.max(1.0, maxHealth);
    }

    public double getHealthRatio() {
        if (boss.isDead()) return 0.0;
        return Math.max(0.0, Math.min(1.0, boss.getHealth() / maxHealth));
    }

    public boolean isDead() {
        return boss.isDead() || boss.getHealth() <= 0.0;
    }
}
