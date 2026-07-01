package com.ultimatedungeon.boss.abilities;

import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/** Hurls a fireball at the nearest player. */
public final class ProjectileAbility extends AbstractBossAbility {
    public ProjectileAbility(@NotNull final String id, final double damage, final long cooldownTicks, final double range) {
        super(id, damage, cooldownTicks, range);
    }
    @Override
    protected void perform(@NotNull final LivingEntity boss) {
        final Player target = nearestPlayer(boss);
        if (target == null) return;
        final Vector dir = target.getEyeLocation().toVector().subtract(boss.getEyeLocation().toVector());
        if (dir.lengthSquared() < 1.0E-4) return;
        boss.launchProjectile(SmallFireball.class, dir.normalize());
        if (boss.getWorld() != null) {
            boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);
        }
    }
}
