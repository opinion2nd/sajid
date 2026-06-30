package com.ultimatedungeon.boss.abilities;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/** Summons a pack of minions around the boss. */
public final class SummonAbility extends AbstractBossAbility {
    private final int count;
    public SummonAbility(@NotNull final String id, final double damage, final long cooldownTicks, final double range) {
        super(id, damage, cooldownTicks, range);
        this.count = 3;
    }
    @Override
    protected void perform(@NotNull final LivingEntity boss) {
        if (boss.getWorld() == null) return;
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.6f, 1.2f);
        for (int i = 0; i < count; i++) {
            boss.getWorld().spawnEntity(boss.getLocation().clone().add(
                    Math.random() * 4 - 2, 0, Math.random() * 4 - 2), EntityType.ZOMBIE);
        }
    }
}
