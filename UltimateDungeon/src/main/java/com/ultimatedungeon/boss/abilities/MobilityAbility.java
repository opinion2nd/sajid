package com.ultimatedungeon.boss.abilities;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/** Teleport-charges to the nearest player and strikes. */
public final class MobilityAbility extends AbstractBossAbility {
    public MobilityAbility(@NotNull final String id, final double damage, final long cooldownTicks, final double range) {
        super(id, damage, cooldownTicks, range);
    }
    @Override
    protected void perform(@NotNull final LivingEntity boss) {
        final Player target = nearestPlayer(boss);
        if (target == null) return;
        if (boss.getWorld() != null) {
            boss.getWorld().spawnParticle(Particle.SMOKE, boss.getLocation(), 30, 0.4, 0.8, 0.4, 0.0);
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.7f);
        }
        boss.teleport(target.getLocation());
        if (damage > 0) target.damage(damage, boss);
    }
}
