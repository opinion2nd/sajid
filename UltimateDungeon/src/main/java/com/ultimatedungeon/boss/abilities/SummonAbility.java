package com.ultimatedungeon.boss.abilities;

import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/** Summons a mixed pack of minions around the boss. */
public final class SummonAbility extends AbstractBossAbility {
    private static final EntityType[] MINIONS = {
            EntityType.ZOMBIE, EntityType.SKELETON, EntityType.SPIDER,
            EntityType.HUSK, EntityType.STRAY
    };
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
            final EntityType type = MINIONS[ThreadLocalRandom.current().nextInt(MINIONS.length)];
            boss.getWorld().spawnEntity(boss.getLocation().clone().add(
                    ThreadLocalRandom.current().nextDouble(-2, 2), 0,
                    ThreadLocalRandom.current().nextDouble(-2, 2)), type);
        }
    }
}
